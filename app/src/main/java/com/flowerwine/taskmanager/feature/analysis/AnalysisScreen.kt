package com.flowerwine.taskmanager.feature.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowerwine.taskmanager.core.model.AnalysisRange
import com.flowerwine.taskmanager.ui.components.TmBarChart
import com.flowerwine.taskmanager.ui.components.TmChartLabels
import com.flowerwine.taskmanager.ui.components.TmDonutChart
import com.flowerwine.taskmanager.ui.components.TmLineChart
import com.flowerwine.taskmanager.ui.components.TmSegmentedTabs
import com.flowerwine.taskmanager.ui.components.AppIcon

@Composable
private fun SectionIcon(
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun AnalysisScreen(
    state: AnalysisUiState,
    onRangeSelected: (AnalysisRange) -> Unit,
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
    val options = AnalysisRange.entries.map { it.label }

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
                            imageVector = Icons.Outlined.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Column {
                        Text(
                            text = "分析",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "趋势与变化",
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
            TmSegmentedTabs(
                options = options,
                selectedIndex = AnalysisRange.entries.indexOf(dashboard.selectedRange),
                onSelected = { onRangeSelected(AnalysisRange.entries[it]) },
            )
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
                    SectionIcon(
                        icon = Icons.Outlined.Memory,
                        backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.1f),
                        iconTint = Color(0xFF3B82F6),
                    )
                    Text(
                        text = "内存趋势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "平均可用内存",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = dashboard.memoryAverageLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3B82F6),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "低内存提醒",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = dashboard.lowMemoryAlertsLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF7A00),
                        )
                    }
                }
                
                // 处理数据量过多的情况：如果数据点超过10个，使用横向滚动
                if (dashboard.memoryTrend.size > 10) {
                    val scrollState = rememberScrollState()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .horizontalScroll(scrollState)
                                .width((dashboard.memoryTrend.size * 50).dp)
                        ) {
                            TmLineChart(points = dashboard.memoryTrend)
                        }
                        Box(
                            modifier = Modifier
                                .horizontalScroll(scrollState)
                                .width((dashboard.memoryTrend.size * 50).dp)
                        ) {
                            TmChartLabels(dashboard.memoryTrend.map { it.label })
                        }
                    }
                } else {
                    TmLineChart(points = dashboard.memoryTrend)
                    TmChartLabels(dashboard.memoryTrend.map { it.label })
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SectionIcon(
                            icon = Icons.Outlined.DeviceThermostat,
                            backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f),
                            iconTint = Color(0xFF10B981),
                        )
                        Text(
                            text = "温度变化",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "最高 ${dashboard.peakTemperatureLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                
                // 处理数据量过多的情况
                if (dashboard.temperatureTrend.size > 10) {
                    val scrollState = rememberScrollState()
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .horizontalScroll(scrollState)
                                .width((dashboard.temperatureTrend.size * 50).dp)
                        ) {
                            TmLineChart(points = dashboard.temperatureTrend, highlightOrange = true)
                        }
                        Box(
                            modifier = Modifier
                                .horizontalScroll(scrollState)
                                .width((dashboard.temperatureTrend.size * 50).dp)
                        ) {
                            TmChartLabels(dashboard.temperatureTrend.map { it.label })
                        }
                    }
                } else {
                    TmLineChart(points = dashboard.temperatureTrend, highlightOrange = true)
                    TmChartLabels(dashboard.temperatureTrend.map { it.label })
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SectionIcon(
                            icon = Icons.Outlined.Wifi,
                            backgroundColor = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                            iconTint = Color(0xFF8B5CF6),
                        )
                        Text(
                            text = "流量统计",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6)),
                            )
                            Text(
                                text = "Wi-Fi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981)),
                            )
                            Text(
                                text = "移动数据",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    
                    // 处理数据量过多的情况
                    if (dashboard.networkTrend.size > 10) {
                        val scrollState = rememberScrollState()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .horizontalScroll(scrollState)
                                    .width((dashboard.networkTrend.size * 50).dp)
                            ) {
                                TmBarChart(items = dashboard.networkTrend)
                            }
                            Box(
                                modifier = Modifier
                                    .horizontalScroll(scrollState)
                                    .width((dashboard.networkTrend.size * 50).dp)
                            ) {
                                TmChartLabels(dashboard.networkTrend.map { it.label })
                            }
                        }
                    } else {
                        TmBarChart(items = dashboard.networkTrend)
                        TmChartLabels(dashboard.networkTrend.map { it.label })
                    }
                    
                    Column {
                        Text(
                            text = "总计",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "16.8 GB",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "3.6 GB",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SectionIcon(
                            icon = Icons.Outlined.PieChart,
                            backgroundColor = Color(0xFFFF7A00).copy(alpha = 0.1f),
                            iconTint = Color(0xFFFF7A00),
                        )
                        Text(
                            text = "使用时长分布",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    TmDonutChart(slices = dashboard.usageDistribution)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        dashboard.usageDistribution.forEach { slice ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AppIcon(
                                    packageName = slice.packageName,
                                    displayName = slice.label,
                                    modifier = Modifier.height(28.dp),
                                    size = 28.dp,
                                )
                                Text(
                                    text = slice.displayValue,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
