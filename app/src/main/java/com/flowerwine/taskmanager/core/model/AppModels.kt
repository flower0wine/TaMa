package com.flowerwine.taskmanager.core.model

enum class AppSortOption(val label: String) {
    MostUsed("常用"),
    StorageHeavy("占空间"),
    NetworkHeavy("耗流量"),
}

data class AppListItem(
    val packageName: String,
    val displayName: String,
    val isSystemApp: Boolean,
    val usageLabel: String,
    val lastUsedLabel: String,
    val storageLabel: String?,
    val networkLabel: String?,
    val score: Long,
)

data class AppOverviewSummary(
    val activeAppsLabel: String,
    val totalUsageLabel: String,
    val foregroundSwitchesLabel: String,
)

data class SelectedAppSummary(
    val packageName: String,
    val displayName: String,
    val todayUsageLabel: String,
    val lastUsedLabel: String,
    val storageLabel: String,
    val networkLabel: String,
)

data class AppsDashboard(
    val sortOption: AppSortOption,
    val includeSystemApps: Boolean,
    val overview: AppOverviewSummary,
    val apps: List<AppListItem>,
    val selectedApp: SelectedAppSummary?,
    val usagePermissionGranted: Boolean,
)
