package com.flowerwine.taskmanager.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.snapshotDataStore by preferencesDataStore(name = "snapshot_store")

class SnapshotStore(private val context: Context) {

    private val snapshotsKey = stringPreferencesKey("device_snapshots")

    suspend fun append(snapshot: DeviceSnapshotEntity) {
        val snapshots = getAll().toMutableList().apply {
            add(snapshot)
            sortBy { it.capturedAt }
            while (size > MaxSnapshots) removeFirst()
        }
        persist(snapshots)
    }

    suspend fun getSince(startTime: Long): List<DeviceSnapshotEntity> {
        return getAll().filter { it.capturedAt >= startTime }
    }

    suspend fun getLatest(): DeviceSnapshotEntity? {
        return getAll().maxByOrNull { it.capturedAt }
    }

    private suspend fun getAll(): List<DeviceSnapshotEntity> {
        val value = context.snapshotDataStore.data.first()[snapshotsKey] ?: return emptyList()
        val jsonArray = JSONArray(value)
        return buildList(jsonArray.length()) {
            repeat(jsonArray.length()) { index ->
                val item = jsonArray.getJSONObject(index)
                add(
                    DeviceSnapshotEntity(
                        capturedAt = item.getLong("capturedAt"),
                        availableMemoryBytes = item.getLong("availableMemoryBytes"),
                        totalMemoryBytes = item.getLong("totalMemoryBytes"),
                        batteryLevelPercent = item.getInt("batteryLevelPercent"),
                        batteryTemperatureCelsius = item.optDouble("batteryTemperatureCelsius")
                            .takeIf { !item.isNull("batteryTemperatureCelsius") }
                            ?.toFloat(),
                        thermalStatus = item.getInt("thermalStatus"),
                        storageFreeBytes = item.getLong("storageFreeBytes"),
                        storageTotalBytes = item.getLong("storageTotalBytes"),
                        wifiBytes = item.getLong("wifiBytes"),
                        mobileBytes = item.getLong("mobileBytes"),
                    ),
                )
            }
        }
    }

    private suspend fun persist(snapshots: List<DeviceSnapshotEntity>) {
        val payload = JSONArray().apply {
            snapshots.forEach { snapshot ->
                put(
                    JSONObject().apply {
                        put("capturedAt", snapshot.capturedAt)
                        put("availableMemoryBytes", snapshot.availableMemoryBytes)
                        put("totalMemoryBytes", snapshot.totalMemoryBytes)
                        put("batteryLevelPercent", snapshot.batteryLevelPercent)
                        put("batteryTemperatureCelsius", snapshot.batteryTemperatureCelsius)
                        put("thermalStatus", snapshot.thermalStatus)
                        put("storageFreeBytes", snapshot.storageFreeBytes)
                        put("storageTotalBytes", snapshot.storageTotalBytes)
                        put("wifiBytes", snapshot.wifiBytes)
                        put("mobileBytes", snapshot.mobileBytes)
                    },
                )
            }
        }.toString()

        context.snapshotDataStore.edit { preferences ->
            preferences[snapshotsKey] = payload
        }
    }

    private companion object {
        const val MaxSnapshots = 120
    }
}
