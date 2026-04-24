package com.flowerwine.taskmanager.feature.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowerwine.taskmanager.core.model.ToolActionType

@Composable
fun ToolsScreen(
    state: ToolsUiState,
    onAction: (ToolActionType) -> Unit,
) {
    if (state.isLoading && state.dashboard == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val dashboard = state.dashboard ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Column {
                        Text(
                            text = "工具",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "建议与快捷入口",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    Icons.Outlined.NotificationsNone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "💡", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "智能建议",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                dashboard.recommendations.forEach { recommendation ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .clickable { onAction(recommendation.actionType) }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(getRecommendationColor(recommendation.title)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = getRecommendationIcon(recommendation.title),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = recommendation.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = recommendation.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = recommendation.actionLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "快捷入口",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    dashboard.shortcuts.chunked(3).forEach { rowShortcuts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowShortcuts.forEach { shortcut ->
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .clickable { onAction(shortcut.actionType) }
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(getShortcutColor(shortcut.title)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = getShortcutIcon(shortcut.title),
                                            style = MaterialTheme.typography.titleMedium,
                                        )
                                    }
                                    Text(
                                        text = shortcut.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = shortcut.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = "权限状态",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                dashboard.permissionItems.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = getPermissionIcon(item.label),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (item.enabled) "已开启" else "未开启",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (item.enabled) Color(0xFF10B981) else Color(0xFFFF7A00),
                                fontWeight = FontWeight.Medium,
                            )
                            Icon(
                                Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = "ℹ️", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "说明",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = "本应用提供设备状态分析与建议，不会直接结束其他应用后台进程，也不会伪装成系统管家。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun getRecommendationColor(title: String): Color {
    return when {
        title.contains("温度") -> Color(0xFFFF7A00).copy(alpha = 0.2f)
        title.contains("微信") || title.contains("应用") -> Color(0xFF10B981).copy(alpha = 0.2f)
        else -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
    }
}

@Composable
private fun getRecommendationIcon(title: String): String {
    return when {
        title.contains("温度") -> "🌡️"
        title.contains("微信") || title.contains("应用") -> "💬"
        else -> "📊"
    }
}

@Composable
private fun getShortcutColor(title: String): Color {
    return when {
        title.contains("电池") -> Color(0xFF10B981).copy(alpha = 0.2f)
        title.contains("应用") -> Color(0xFF3B82F6).copy(alpha = 0.2f)
        title.contains("存储") -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
        title.contains("通知") -> Color(0xFFFF7A00).copy(alpha = 0.2f)
        title.contains("使用") -> Color(0xFF06B6D4).copy(alpha = 0.2f)
        title.contains("网络") -> Color(0xFF3B82F6).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.primaryContainer
    }
}

@Composable
private fun getShortcutIcon(title: String): String {
    return when {
        title.contains("电池") -> "🔋"
        title.contains("应用") -> "📱"
        title.contains("存储") -> "💾"
        title.contains("通知") -> "🔔"
        title.contains("使用") -> "👤"
        title.contains("网络") -> "📊"
        else -> "⚙️"
    }
}

@Composable
private fun getPermissionIcon(label: String): String {
    return when {
        label.contains("使用") -> "👤"
        label.contains("通知") -> "🔔"
        label.contains("悬浮") -> "📱"
        else -> "⚙️"
    }
}
