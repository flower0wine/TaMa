package com.flowerwine.taskmanager.domain.usecase

import com.flowerwine.taskmanager.core.model.PermissionStatus
import com.flowerwine.taskmanager.core.model.ToolActionType
import com.flowerwine.taskmanager.core.model.ToolRecommendation
import com.flowerwine.taskmanager.data.system.DeviceSnapshot

class BuildToolRecommendationsUseCase {

    operator fun invoke(
        permissionStatus: PermissionStatus,
        snapshot: DeviceSnapshot,
        heaviestApp: String?,
    ): List<ToolRecommendation> {
        val result = mutableListOf<ToolRecommendation>()

        if ((snapshot.batteryTemperatureCelsius ?: 0f) >= 40f) {
            result += ToolRecommendation(
                title = "温度偏高",
                description = "当前电池温度偏高，建议暂时停止高负载使用，并保持良好散热。",
                actionLabel = "查看建议",
                actionType = ToolActionType.OpenAppDetails,
            )
        }
        if (!permissionStatus.usageAccessGranted) {
            result += ToolRecommendation(
                title = "应用使用分析未开启",
                description = "开启使用情况访问后，才能统计应用时长、活跃度和趋势图。",
                actionLabel = "去开启",
                actionType = ToolActionType.OpenUsageAccess,
            )
        }
        if (heaviestApp != null) {
            result += ToolRecommendation(
                title = "$heaviestApp 占空间较大",
                description = "建议检查缓存和下载目录，必要时跳转系统存储页进一步处理。",
                actionLabel = "去查看",
                actionType = ToolActionType.OpenStorageSettings,
            )
        }
        if (!permissionStatus.overlayGranted) {
            result += ToolRecommendation(
                title = "悬浮窗未开启",
                description = "如果后续启用浮层提醒或快捷入口，需要先开启悬浮窗权限。",
                actionLabel = "去开启",
                actionType = ToolActionType.OpenOverlaySettings,
            )
        }

        return result.take(3)
    }
}
