package com.flowerwine.taskmanager.core.model

data class ToolRecommendation(
    val title: String,
    val description: String,
    val actionLabel: String,
    val actionType: ToolActionType,
)

data class ToolShortcut(
    val title: String,
    val description: String,
    val actionType: ToolActionType,
)

data class PermissionStatusItem(
    val label: String,
    val enabled: Boolean,
)

enum class ToolActionType {
    OpenUsageAccess,
    OpenOverlaySettings,
    OpenAppNotificationSettings,
    OpenAppDetails,
    OpenDataUsageSettings,
    OpenStorageSettings,
}

data class ToolsDashboard(
    val recommendations: List<ToolRecommendation>,
    val shortcuts: List<ToolShortcut>,
    val permissionItems: List<PermissionStatusItem>,
)
