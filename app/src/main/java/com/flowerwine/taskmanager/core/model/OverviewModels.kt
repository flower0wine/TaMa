package com.flowerwine.taskmanager.core.model

data class PermissionStatus(
    val usageAccessGranted: Boolean,
    val notificationsGranted: Boolean,
    val overlayGranted: Boolean,
)

data class MetricCardValue(
    val title: String,
    val primaryLabel: String,
    val secondaryLabel: String,
    val accent: MetricAccent,
)

enum class MetricAccent {
    Blue,
    Green,
    Orange,
    Purple,
    Red,
    Neutral,
}

data class InsightCard(
    val title: String,
    val body: String,
    val actionLabel: String,
)

data class OverviewDashboard(
    val memory: MetricCardValue,
    val battery: MetricCardValue,
    val network: MetricCardValue,
    val storage: MetricCardValue,
    val insight: InsightCard,
    val topApps: List<AppListItem>,
    val permissions: PermissionStatus,
)
