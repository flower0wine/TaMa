package com.flowerwine.taskmanager.data.repository

import com.flowerwine.taskmanager.core.model.AnalysisDashboard
import com.flowerwine.taskmanager.core.model.AnalysisRange
import com.flowerwine.taskmanager.core.model.AppListItem
import com.flowerwine.taskmanager.core.model.BarGroup
import com.flowerwine.taskmanager.core.model.ChartPoint
import com.flowerwine.taskmanager.core.model.InsightCard
import com.flowerwine.taskmanager.core.model.MetricAccent
import com.flowerwine.taskmanager.core.model.MetricCardValue
import com.flowerwine.taskmanager.core.model.OverviewDashboard
import com.flowerwine.taskmanager.core.model.UsageSlice
import com.flowerwine.taskmanager.core.util.formatBytes
import com.flowerwine.taskmanager.core.util.formatRelativeMinutes
import com.flowerwine.taskmanager.core.util.formatTemperature
import com.flowerwine.taskmanager.core.util.formatUsageDuration
import com.flowerwine.taskmanager.data.local.DeviceSnapshotEntity
import com.flowerwine.taskmanager.data.local.SnapshotStore
import com.flowerwine.taskmanager.data.preferences.UserPreferencesRepository
import com.flowerwine.taskmanager.data.system.DeviceSnapshot
import com.flowerwine.taskmanager.data.system.DeviceStatusDataSource
import com.flowerwine.taskmanager.data.system.PermissionStatusDataSource
import com.flowerwine.taskmanager.data.system.UsageStatsDataSource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToLong

class DeviceRepository(
    private val snapshotStore: SnapshotStore,
    private val deviceStatusDataSource: DeviceStatusDataSource,
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
        val permissions = permissionStatusDataSource.getPermissionStatus()
        val usageRecords = usageStatsDataSource.queryUsageRecords(startOfToday(), System.currentTimeMillis())
        val topApps = usageRecords.take(3).map { record ->
            AppListItem(
                packageName = record.packageName,
                displayName = record.packageName.substringAfterLast('.'),
                usageLabel = formatUsageDuration(record.totalForegroundTimeMillis),
                lastUsedLabel = formatRelativeMinutes(System.currentTimeMillis() - record.lastTimeUsed),
                storageLabel = null,
                networkLabel = null,
                score = record.totalForegroundTimeMillis,
            )
        }

        return OverviewDashboard(
            memory = MetricCardValue(
                title = "内存状态",
                primaryLabel = formatBytes(snapshot.availableMemoryBytes),
                secondaryLabel = "总内存 ${formatBytes(snapshot.totalMemoryBytes)}",
                accent = MetricAccent.Blue,
            ),
            battery = MetricCardValue(
                title = "电池与温度",
                primaryLabel = "${snapshot.batteryLevelPercent}%",
                secondaryLabel = formatTemperature(snapshot.batteryTemperatureCelsius),
                accent = if ((snapshot.batteryTemperatureCelsius ?: 0f) >= 40f) MetricAccent.Orange else MetricAccent.Green,
            ),
            network = MetricCardValue(
                title = "今日流量",
                primaryLabel = formatBytes(snapshot.wifiBytes),
                secondaryLabel = "移动数据 ${formatBytes(snapshot.mobileBytes)}",
                accent = MetricAccent.Green,
            ),
            storage = MetricCardValue(
                title = "存储空间",
                primaryLabel = formatBytes(snapshot.storageTotalBytes - snapshot.storageFreeBytes),
                secondaryLabel = "可用 ${formatBytes(snapshot.storageFreeBytes)}",
                accent = MetricAccent.Purple,
            ),
            insight = buildInsight(snapshot, permissions.usageAccessGranted),
            topApps = topApps,
            permissions = permissions,
        )
    }

    suspend fun getAnalysisDashboard(range: AnalysisRange): AnalysisDashboard {
        val latestSnapshot = captureSnapshot()
        val startTime = range.startMillis()
        val snapshots = snapshotStore.getSince(startTime).ifEmpty { listOf(latestSnapshot) }
        val usageRecords = usageStatsDataSource.queryUsageRecords(startTime, System.currentTimeMillis())
        val topUsage = usageRecords.take(4)
        val totalUsage = topUsage.sumOf { it.totalForegroundTimeMillis }.coerceAtLeast(1L)

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
            usageDistribution = topUsage.map { record ->
                UsageSlice(
                    label = record.packageName.substringAfterLast('.'),
                    value = record.totalForegroundTimeMillis.toFloat() / totalUsage,
                    displayValue = formatUsageDuration(record.totalForegroundTimeMillis),
                )
            },
            memoryAverageLabel = formatBytes(
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
    }
}
