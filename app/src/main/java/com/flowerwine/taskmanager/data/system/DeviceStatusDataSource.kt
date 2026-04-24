package com.flowerwine.taskmanager.data.system

import android.app.ActivityManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.os.storage.StorageManager
import java.time.LocalDate
import java.time.ZoneId

data class DeviceSnapshot(
    val capturedAt: Long,
    val availableMemoryBytes: Long,
    val totalMemoryBytes: Long,
    val batteryLevelPercent: Int,
    val batteryTemperatureCelsius: Float?,
    val thermalStatus: Int,
    val storageFreeBytes: Long,
    val storageTotalBytes: Long,
    val wifiBytes: Long,
    val mobileBytes: Long,
)

class DeviceStatusDataSource(private val context: Context) {

    private val activityManager = context.getSystemService(ActivityManager::class.java)
    private val batteryManager = context.getSystemService(BatteryManager::class.java)
    private val powerManager = context.getSystemService(PowerManager::class.java)
    private val storageStatsManager = context.getSystemService(StorageStatsManager::class.java)
    private val networkStatsManager = context.getSystemService(NetworkStatsManager::class.java)

    fun readSnapshot(): DeviceSnapshot {
        val memoryInfo = ActivityManager.MemoryInfo().also(activityManager::getMemoryInfo)
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).takeIf { it in 0..100 }
            ?: batteryStatus?.let { intent ->
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) level * 100 / scale else 0
            } ?: 0
        val batteryTemperature = batteryStatus
            ?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
            ?.takeIf { it != Int.MIN_VALUE }
            ?.div(10f)
        val thermalStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            powerManager.currentThermalStatus
        } else {
            0
        }
        val storageUuid = StorageManager.UUID_DEFAULT

        return DeviceSnapshot(
            capturedAt = System.currentTimeMillis(),
            availableMemoryBytes = memoryInfo.availMem,
            totalMemoryBytes = memoryInfo.totalMem,
            batteryLevelPercent = batteryLevel,
            batteryTemperatureCelsius = batteryTemperature,
            thermalStatus = thermalStatus,
            storageFreeBytes = runCatching { storageStatsManager.getFreeBytes(storageUuid) }.getOrDefault(-1),
            storageTotalBytes = runCatching { storageStatsManager.getTotalBytes(storageUuid) }.getOrDefault(-1),
            wifiBytes = queryDeviceNetworkBytes(ConnectivityManager.TYPE_WIFI),
            mobileBytes = queryDeviceNetworkBytes(ConnectivityManager.TYPE_MOBILE),
        )
    }

    fun queryAppStorageBytes(packageName: String): Long? {
        return runCatching {
            val stats = storageStatsManager.queryStatsForPackage(
                StorageManager.UUID_DEFAULT,
                packageName,
                Process.myUserHandle(),
            )
            stats.appBytes + stats.dataBytes + stats.cacheBytes
        }.getOrNull()
    }

    fun queryAppNetworkBytes(uid: Int): Long? {
        val wifiBytes = queryUidNetworkBytes(ConnectivityManager.TYPE_WIFI, uid)
        val mobileBytes = queryUidNetworkBytes(ConnectivityManager.TYPE_MOBILE, uid)
        return listOf(wifiBytes, mobileBytes)
            .takeIf { it.any { value -> value >= 0 } }
            ?.filter { it >= 0 }
            ?.sum()
    }

    private fun queryDeviceNetworkBytes(networkType: Int): Long {
        val bucket = runCatching {
            networkStatsManager.querySummaryForDevice(networkType, null, startOfToday(), System.currentTimeMillis())
        }.getOrNull() ?: return -1
        return bucket.rxBytes + bucket.txBytes
    }

    private fun queryUidNetworkBytes(networkType: Int, uid: Int): Long {
        return runCatching {
            val stats = networkStatsManager.queryDetailsForUidTagState(
                networkType,
                null,
                startOfToday(),
                System.currentTimeMillis(),
                uid,
                NetworkStats.Bucket.TAG_NONE,
                NetworkStats.Bucket.STATE_ALL,
            )
            var totalBytes = 0L
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                totalBytes += bucket.rxBytes + bucket.txBytes
            }
            stats.close()
            totalBytes
        }.getOrDefault(-1)
    }

    private fun startOfToday(): Long = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
