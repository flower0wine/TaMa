package com.flowerwine.taskmanager.data.repository

import android.os.PowerManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingFlat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Thermostat
import androidx.compose.material.icons.outlined.WarningAmber
import com.flowerwine.taskmanager.core.model.AnalysisDashboard
import com.flowerwine.taskmanager.core.model.AnalysisRange
import com.flowerwine.taskmanager.core.model.AppListItem
import com.flowerwine.taskmanager.core.model.BarGroup
import com.flowerwine.taskmanager.core.model.ChartPoint
import com.flowerwine.taskmanager.core.model.InsightCard
import com.flowerwine.taskmanager.core.model.MetricAccent
import com.flowerwine.taskmanager.core.model.MetricCardValue
import com.flowerwine.taskmanager.core.model.MetricTrend
import com.flowerwine.taskmanager.core.model.OverviewDashboard
import com.flowerwine.taskmanager.core.model.UsageSlice
import com.flowerwine.taskmanager.core.util.formatBytes
import com.flowerwine.taskmanager.core.util.formatBytesDecimal
import com.flowerwine.taskmanager.core.util.formatCompactUsageDuration
import com.flowerwine.taskmanager.core.util.formatPercentage
import com.flowerwine.taskmanager.core.util.formatRelativeMinutes
import com.flowerwine.taskmanager.core.util.formatTemperature
import com.flowerwine.taskmanager.core.util.formatUsageDuration
import com.flowerwine.taskmanager.data.local.DeviceSnapshotEntity
import com.flowerwine.taskmanager.data.local.SnapshotStore
import com.flowerwine.taskmanager.data.preferences.UserPreferencesRepository
import com.flowerwine.taskmanager.data.system.DeviceSnapshot
import com.flowerwine.taskmanager.data.system.DeviceStatusDataSource
import com.flowerwine.taskmanager.data.system.InstalledAppsDataSource
import com.flowerwine.taskmanager.data.system.PermissionStatusDataSource
import com.flowerwine.taskmanager.data.system.UsageStatsDataSource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue
import kotlin.math.roundToLong

class DeviceRepository(
    private val snapshotStore: SnapshotStore,
    private val deviceStatusDataSource: DeviceStatusDataSource,
    private val installedAppsDataSource: InstalledAppsDataSource,
    private val usageStatsDataSource: UsageStatsDataSource,
    private val permissionStatusDataSource: PermissionStatusDataSource,
    private val preferencesRepository: UserPreferencesRepository,
) {

    val selectedAnalysisRange: Flow<AnalysisRange> = preferencesRepository.analysisRange

    suspend fun setAnalysisRange(range: AnalysisRange) {
        preferencesRepository.setAnalysisRange(range)
    }

    suspend fun captureSnapshot(): DeviceSnapshotEntity {
        val snapshot = deviceStatusDataSource.readSnapshot()
        val entity = snapshot.toEntity()
        snapshotStore.append(entity)
        return entity
    }

    suspend fun getOverviewDashboard(): OverviewDashboard {
        val snapshot = captureSnapshot()
        val recentSnapshots = snapshotStore.getSince(System.currentTimeMillis() - ThirtyMinutesMillis)
        val permissions = permissionStatusDataSource.getPermissionStatus()
        val launcherApps = installedAppsDataSource.getLauncherApps()
        val launcherAppsByPackage = launcherApps.associateBy { it.packageName }
        val usageRecords = usageStatsDataSource.queryUsageRecords(startOfToday(), System.currentTimeMillis())
        val topApps = usageRecords
            .mapNotNull { record ->
                val launcherApp = launcherAppsByPackage[record.packageName] ?: return@mapNotNull null
                AppListItem(
                    packageName = record.packageName,
                    displayName = launcherApp.displayName,
                    isSystemApp = launcherApp.isSystemApp,
                    usageLabel = formatUsageDuration(record.totalForegroundTimeMillis),
                    lastUsedLabel = formatRelativeMinutes(System.currentTimeMillis() - record.lastTimeUsed),
                    storageLabel = null,
                    networkLabel = null,
                    score = record.totalForegroundTimeMillis,
                )
            }
            .take(3)

        return OverviewDashboard(
            memory = MetricCardValue(
                title = "内存状态",
                primaryLabel = formatBytesDecimal(snapshot.availableMemoryBytes),
                secondaryLabel = formatBytesDecimal(snapshot.totalMemoryBytes),
                supportingLabel = "已用 ${formatBytesDecimal(snapshot.memoryUsedBytes)} (${formatPercentage(snapshot.memoryUsagePercent)})",
                progress = snapshot.memoryUsageRatio,
                trend = buildMemoryTrend(recentSnapshots),
                accent = MetricAccent.Blue,
            ),
            battery = MetricCardValue(
                title = "电池与温度",
                primaryLabel = "${snapshot.batteryLevelPercent}%",
                secondaryLabel = formatTemperature(snapshot.batteryTemperatureCelsius),
                supportingLabel = buildThermalStatusLabel(snapshot.thermalStatus),
                accent = if ((snapshot.batteryTemperatureCelsius ?: 0f) >= 40f) MetricAccent.Orange else MetricAccent.Green,
            ),
            network = MetricCardValue(
                title = "今日流量",
                primaryLabel = formatBytes(snapshot.wifiBytes),
                secondaryLabel = formatBytes(snapshot.mobileBytes),
                supportingLabel = "总计 ${formatBytes(snapshot.networkTotalBytes)}",
                accent = MetricAccent.Green,
            ),
            storage = MetricCardValue(
                title = "存储空间",
                primaryLabel = formatBytes(snapshot.storageTotalBytes - snapshot.storageFreeBytes),
                secondaryLabel = formatBytes(snapshot.storageFreeBytes),
                supportingLabel = "总共 ${formatBytes(snapshot.storageTotalBytes)} (已用 ${formatPercentage(snapshot.storageUsagePercent)})",
                progress = snapshot.storageUsageRatio,
                accent = MetricAccent.Purple,
            ),
            insight = buildInsight(snapshot, permissions.usageAccessGranted),
            topApps = topApps,
            permissions = permissions,
        )
    }

    private fun buildMemoryTrend(snapshots: List<DeviceSnapshotEntity>): MetricTrend {
        val latest = snapshots.lastOrNull()
        val baseline = snapshots.dropLast(1).lastOrNull()
        if (latest == null || baseline == null) {
            return MetricTrend(
                label = "近30分钟样本不足",
                icon = Icons.Outlined.Info,
            )
        }

        val delta = latest.availableMemoryBytes - baseline.availableMemoryBytes
        val minutes = ((latest.capturedAt - baseline.capturedAt) / TimeUnit.MINUTES.toMillis(1)).coerceAtLeast(1)
        val absDelta = formatBytesDecimal(delta.absoluteValue)

        return when {
            delta < -MemoryStableThresholdBytes -> MetricTrend(
                label = "近${minutes}分钟下降 $absDelta",
                icon = Icons.AutoMirrored.Outlined.TrendingDown,
            )
            delta > MemoryStableThresholdBytes -> MetricTrend(
                label = "近${minutes}分钟回升 $absDelta",
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
            )
            else -> MetricTrend(
                label = "近${minutes}分钟基本稳定",
                icon = Icons.AutoMirrored.Outlined.TrendingFlat,
            )
        }
    }

    private fun buildThermalStatusLabel(thermalStatus: Int): String {
        return when (thermalStatus) {
            PowerManager.THERMAL_STATUS_NONE -> "热状态：正常"
            PowerManager.THERMAL_STATUS_LIGHT -> "热状态：轻微升温"
            PowerManager.THERMAL_STATUS_MODERATE -> "热状态：注意"
            PowerManager.THERMAL_STATUS_SEVERE -> "热状态：偏高"
            PowerManager.THERMAL_STATUS_CRITICAL -> "热状态：严重"
            PowerManager.THERMAL_STATUS_EMERGENCY -> "热状态：紧急"
            PowerManager.THERMAL_STATUS_SHUTDOWN -> "热状态：接近关机"
            else -> "热状态：待获取"
        }
    }

    suspend fun getAnalysisDashboard(range: AnalysisRange): AnalysisDashboard {
        val latestSnapshot = captureSnapshot()
        val startTime = range.startMillis()
        val snapshots = snapshotStore.getSince(startTime).ifEmpty { listOf(latestSnapshot) }
        val launcherAppsByPackage = installedAppsDataSource.getLauncherApps().associateBy { it.packageName }
        val usageRecords = usageStatsDataSource.queryUsageRecords(startTime, System.currentTimeMillis())
        val topUsage = usageRecords
            .mapNotNull { record ->
                val launcherApp = launcherAppsByPackage[record.packageName] ?: return@mapNotNull null
                record to launcherApp
            }
            .take(4)
        val totalUsage = topUsage.sumOf { (record, _) -> record.totalForegroundTimeMillis }.coerceAtLeast(1L)

        return AnalysisDashboard(
            selectedRange = range,
            memoryTrend = snapshots.mapIndexed { index, snapshot ->
                ChartPoint(snapshot.chartLabel(range, index), snapshot.availableMemoryBytes.toFloat() / GB)
            },
            temperatureTrend = snapshots.mapIndexed { index, snapshot ->
                ChartPoint(snapshot.chartLabel(range, index), snapshot.batteryTemperatureCelsius ?: 0f)
            },
            networkTrend = snapshots.mapIndexed { index, snapshot ->
                BarGroup(
                    label = snapshot.chartLabel(range, index),
                    primaryValue = snapshot.wifiBytes.toFloat() / GB,
                    secondaryValue = snapshot.mobileBytes.toFloat() / GB,
                )
            },
            usageDistribution = topUsage.map { (record, launcherApp) ->
                UsageSlice(
                    packageName = record.packageName,
                    label = launcherApp.displayName,
                    value = record.totalForegroundTimeMillis.toFloat() / totalUsage,
                    displayValue = formatCompactUsageDuration(record.totalForegroundTimeMillis),
                )
            },
            memoryAverageLabel = formatBytesDecimal(
                snapshots.map { it.availableMemoryBytes }.average().roundToLong(),
            ),
            lowMemoryAlertsLabel = snapshots.count { it.availableMemoryBytes < (it.totalMemoryBytes * 0.15f) }.toString(),
            peakTemperatureLabel = formatTemperature(snapshots.maxOfOrNull { it.batteryTemperatureCelsius ?: 0f }),
            insightTitle = "本周洞察",
            insightBody = buildAnalysisInsight(snapshots, usageRecords),
        )
    }

    private fun buildInsight(snapshot: DeviceSnapshotEntity, usageGranted: Boolean): InsightCard {
        return when {
            !usageGranted -> InsightCard(
                title = "应用分析尚未开启",
                body = "开启使用情况访问后，才能统计活跃应用、前台切换和趋势图。",
                actionLabel = "去开启",
            )
            (snapshot.batteryTemperatureCelsius ?: 0f) >= 40f -> InsightCard(
                title = "当前建议",
                body = "当前温度偏高，建议暂停高负载使用，并避免边充边玩。",
                actionLabel = "查看建议",
            )
            snapshot.storageFreeBytes in 0 until 15L * 1024 * 1024 * 1024 -> InsightCard(
                title = "当前建议",
                body = "剩余存储较少，建议优先清理缓存和下载内容。",
                actionLabel = "查看存储",
            )
            else -> InsightCard(
                title = "当前建议",
                body = "设备状态整体稳定，可继续观察近 7 天温度与内存趋势。",
                actionLabel = "查看分析",
            )
        }
    }

    private fun buildAnalysisInsight(
        snapshots: List<DeviceSnapshotEntity>,
        usageRecords: List<com.flowerwine.taskmanager.data.system.AppUsageRecord>,
    ): String {
        val peakTemp = snapshots.maxOfOrNull { it.batteryTemperatureCelsius ?: 0f } ?: 0f
        val dominantApp = usageRecords.maxByOrNull { it.totalForegroundTimeMillis }
            ?.packageName
            ?.substringAfterLast('.')

        return when {
            peakTemp >= 40f -> "区间内最高电池温度达到 ${formatTemperature(peakTemp)}，建议减少边充边高负载场景。"
            dominantApp != null -> "$dominantApp 是当前阶段最活跃应用，建议结合应用页继续观察资源占用。"
            else -> "当前样本还较少，继续使用几天后趋势会更稳定。"
        }
    }

    private fun DeviceSnapshot.toEntity(): DeviceSnapshotEntity = DeviceSnapshotEntity(
        capturedAt = capturedAt,
        availableMemoryBytes = availableMemoryBytes,
        totalMemoryBytes = totalMemoryBytes,
        batteryLevelPercent = batteryLevelPercent,
        batteryTemperatureCelsius = batteryTemperatureCelsius,
        thermalStatus = thermalStatus,
        storageFreeBytes = storageFreeBytes,
        storageTotalBytes = storageTotalBytes,
        wifiBytes = wifiBytes,
        mobileBytes = mobileBytes,
    )

    private fun DeviceSnapshotEntity.chartLabel(range: AnalysisRange, index: Int): String {
        return when (range) {
            AnalysisRange.Today -> "${index + 1}"
            AnalysisRange.SevenDays, AnalysisRange.ThirtyDays -> {
                val day = java.time.Instant.ofEpochMilli(capturedAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                "${day.monthValue}/${day.dayOfMonth}"
            }
        }
    }

    private fun AnalysisRange.startMillis(): Long {
        val zone = ZoneId.systemDefault()
        return when (this) {
            AnalysisRange.Today -> startOfToday()
            AnalysisRange.SevenDays -> LocalDate.now().minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
            AnalysisRange.ThirtyDays -> LocalDate.now().minusDays(29).atStartOfDay(zone).toInstant().toEpochMilli()
        }
    }

    private fun startOfToday(): Long = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    private companion object {
        const val GB = 1024f * 1024f * 1024f
        const val ThirtyMinutesMillis = 30 * 60 * 1000L
        const val MemoryStableThresholdBytes = 100L * 1024 * 1024
    }
}

private val DeviceSnapshotEntity.memoryUsedBytes: Long
    get() = (totalMemoryBytes - availableMemoryBytes).coerceAtLeast(0)

private val DeviceSnapshotEntity.memoryUsageRatio: Float
    get() = if (totalMemoryBytes > 0) {
        (memoryUsedBytes.toFloat() / totalMemoryBytes).coerceIn(0f, 1f)
    } else {
        0f
    }

private val DeviceSnapshotEntity.memoryUsagePercent: Float
    get() = memoryUsageRatio * 100f

private val DeviceSnapshotEntity.networkTotalBytes: Long
    get() = listOf(wifiBytes, mobileBytes)
        .filter { it >= 0 }
        .sum()

private val DeviceSnapshotEntity.storageUsageRatio: Float
    get() = if (storageTotalBytes > 0) {
        ((storageTotalBytes - storageFreeBytes).toFloat() / storageTotalBytes).coerceIn(0f, 1f)
    } else {
        0f
    }

private val DeviceSnapshotEntity.storageUsagePercent: Float
    get() = storageUsageRatio * 100f
