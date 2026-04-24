package com.flowerwine.taskmanager.data.repository

import com.flowerwine.taskmanager.core.model.AppSortOption
import com.flowerwine.taskmanager.core.model.PermissionStatusItem
import com.flowerwine.taskmanager.core.model.ToolActionType
import com.flowerwine.taskmanager.core.model.ToolShortcut
import com.flowerwine.taskmanager.core.model.ToolsDashboard
import com.flowerwine.taskmanager.data.system.DeviceStatusDataSource
import com.flowerwine.taskmanager.data.system.PermissionStatusDataSource
import com.flowerwine.taskmanager.data.system.SettingsNavigator
import com.flowerwine.taskmanager.domain.usecase.BuildToolRecommendationsUseCase

class ToolsRepository(
    private val permissionStatusDataSource: PermissionStatusDataSource,
    private val deviceStatusDataSource: DeviceStatusDataSource,
    private val appsRepository: AppsRepository,
    val settingsNavigator: SettingsNavigator,
    private val buildToolRecommendationsUseCase: BuildToolRecommendationsUseCase,
) {

    suspend fun getDashboard(): ToolsDashboard {
        val permissionStatus = permissionStatusDataSource.getPermissionStatus()
        val snapshot = deviceStatusDataSource.readSnapshot()
        val appDashboard = appsRepository.getDashboard(AppSortOption.StorageHeavy, "")

        return ToolsDashboard(
            recommendations = buildToolRecommendationsUseCase(
                permissionStatus = permissionStatus,
                snapshot = snapshot,
                heaviestApp = appDashboard.apps.firstOrNull()?.displayName,
            ),
            shortcuts = listOf(
                ToolShortcut("电池优化", "查看当前应用的系统详情", ToolActionType.OpenAppDetails),
                ToolShortcut("应用详情", "打开当前应用详情页", ToolActionType.OpenAppDetails),
                ToolShortcut("存储管理", "打开系统存储设置", ToolActionType.OpenStorageSettings),
                ToolShortcut("通知管理", "查看当前应用通知设置", ToolActionType.OpenAppNotificationSettings),
                ToolShortcut("使用情况访问", "开启使用统计权限", ToolActionType.OpenUsageAccess),
                ToolShortcut("网络用量", "打开系统数据用量页", ToolActionType.OpenDataUsageSettings),
            ),
            permissionItems = listOf(
                PermissionStatusItem("使用情况访问", permissionStatus.usageAccessGranted),
                PermissionStatusItem("通知权限", permissionStatus.notificationsGranted),
                PermissionStatusItem("悬浮窗", permissionStatus.overlayGranted),
            ),
        )
    }
}
