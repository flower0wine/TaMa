package com.flowerwine.taskmanager.core.model

import androidx.compose.ui.graphics.vector.ImageVector

data class PermissionStatus(
    val usageAccessGranted: Boolean,
    val notificationsGranted: Boolean,
    val overlayGranted: Boolean,
)

data class MetricCardValue(
    val title: String,
    val primaryLabel: String,
    val secondaryLabel: String,
    val supportingLabel: String? = null,
    val progress: Float? = null,
    val trend: MetricTrend? = null,
    val accent: MetricAccent,
)

data class MetricTrend(
    val label: String,
    val icon: ImageVector,
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
