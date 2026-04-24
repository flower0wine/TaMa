package com.flowerwine.taskmanager.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Overview("overview", "总览", Icons.Outlined.Home),
    Analysis("analysis", "分析", Icons.Outlined.Analytics),
    Apps("apps", "应用", Icons.Outlined.Apps),
    Tools("tools", "工具", Icons.Outlined.Build),
}
