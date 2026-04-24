package com.flowerwine.taskmanager.data.repository

import com.flowerwine.taskmanager.core.model.AppListItem
import com.flowerwine.taskmanager.core.model.AppOverviewSummary
import com.flowerwine.taskmanager.core.model.AppSortOption
import com.flowerwine.taskmanager.core.model.AppsDashboard
import com.flowerwine.taskmanager.core.model.SelectedAppSummary
import com.flowerwine.taskmanager.core.util.formatBytes
import com.flowerwine.taskmanager.core.util.formatRelativeMinutes
import com.flowerwine.taskmanager.core.util.formatUsageDuration
import com.flowerwine.taskmanager.data.preferences.UserPreferencesRepository
import com.flowerwine.taskmanager.data.system.DeviceStatusDataSource
import com.flowerwine.taskmanager.data.system.InstalledAppsDataSource
import com.flowerwine.taskmanager.data.system.PermissionStatusDataSource
import com.flowerwine.taskmanager.data.system.UsageStatsDataSource
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

class AppsRepository(
    private val installedAppsDataSource: InstalledAppsDataSource,
    private val usageStatsDataSource: UsageStatsDataSource,
    private val deviceStatusDataSource: DeviceStatusDataSource,
    private val permissionStatusDataSource: PermissionStatusDataSource,
    private val preferencesRepository: UserPreferencesRepository,
) {

    val selectedSortOption: Flow<AppSortOption> = preferencesRepository.appSort
    val includeSystemApps: Flow<Boolean> = preferencesRepository.includeSystemApps

    suspend fun setSortOption(sortOption: AppSortOption) {
        preferencesRepository.setAppSort(sortOption)
    }

    suspend fun setIncludeSystemApps(includeSystemApps: Boolean) {
        preferencesRepository.setIncludeSystemApps(includeSystemApps)
    }

    suspend fun getDashboard(
        sortOption: AppSortOption,
        query: String,
        includeSystemApps: Boolean,
    ): AppsDashboard {
        val permissionGranted = permissionStatusDataSource.hasUsageAccess()
        val installedApps = installedAppsDataSource.getInstalledApps()
        val usageMap = usageStatsDataSource.queryUsageRecords(startOfToday(), System.currentTimeMillis())
            .associateBy { it.packageName }

        val visibleInstalledApps = installedApps
            .asSequence()
            .filter { includeSystemApps || !it.isSystemApp }
            .toList()
        val visiblePackages = visibleInstalledApps.mapTo(mutableSetOf()) { it.packageName }

        val appItems = visibleInstalledApps
            .asSequence()
            .map { installedApp ->
            val usage = usageMap[installedApp.packageName]
            val storageBytes = if (permissionGranted) deviceStatusDataSource.queryAppStorageBytes(installedApp.packageName) else null
            val networkBytes = if (permissionGranted) deviceStatusDataSource.queryAppNetworkBytes(installedApp.uid) else null
            val score = when (sortOption) {
                AppSortOption.MostUsed -> usage?.totalForegroundTimeMillis ?: 0L
                AppSortOption.StorageHeavy -> storageBytes ?: 0L
                AppSortOption.NetworkHeavy -> networkBytes ?: 0L
            }
            AppListItem(
                packageName = installedApp.packageName,
                displayName = installedApp.displayName,
                isSystemApp = installedApp.isSystemApp,
                usageLabel = formatUsageDuration(usage?.totalForegroundTimeMillis ?: 0L),
                lastUsedLabel = usage?.lastTimeUsed
                    ?.takeIf { it > 0L }
                    ?.let { formatRelativeMinutes(System.currentTimeMillis() - it) }
                    ?: "暂无记录",
                storageLabel = storageBytes?.let(::formatBytes),
                networkLabel = networkBytes?.let(::formatBytes),
                score = score,
            )
        }
            .filter { query.isBlank() || it.displayName.contains(query, ignoreCase = true) }
            .sortedWith(compareByDescending<AppListItem> { it.score }.thenBy { it.displayName.lowercase() })
            .toList()

        val activeRecords = usageMap.values.filter { it.totalForegroundTimeMillis > 0 && it.packageName in visiblePackages }
        val selectedApp = appItems.firstOrNull()?.let { item ->
            SelectedAppSummary(
                packageName = item.packageName,
                displayName = item.displayName,
                todayUsageLabel = item.usageLabel,
                lastUsedLabel = item.lastUsedLabel,
                storageLabel = item.storageLabel ?: "待授权",
                networkLabel = item.networkLabel ?: "待授权",
            )
        }

        return AppsDashboard(
            sortOption = sortOption,
            includeSystemApps = includeSystemApps,
            overview = AppOverviewSummary(
                activeAppsLabel = activeRecords.size.toString(),
                totalUsageLabel = formatUsageDuration(activeRecords.sumOf { it.totalForegroundTimeMillis }),
                foregroundSwitchesLabel = activeRecords.sumOf { it.foregroundCount }.toString(),
            ),
            apps = appItems.take(20),
            selectedApp = selectedApp,
            usagePermissionGranted = permissionGranted,
        )
    }

    private fun startOfToday(): Long = LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
