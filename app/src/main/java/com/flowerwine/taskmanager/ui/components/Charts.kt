package com.flowerwine.taskmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.flowerwine.taskmanager.core.model.BarGroup
import com.flowerwine.taskmanager.core.model.ChartPoint
import com.flowerwine.taskmanager.core.model.UsageSlice
import com.flowerwine.taskmanager.ui.theme.memoryBlue
import com.flowerwine.taskmanager.ui.theme.networkGreen
import com.flowerwine.taskmanager.ui.theme.storagePurple
import com.flowerwine.taskmanager.ui.theme.thermalOrange

@Composable
fun TmLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
    highlightOrange: Boolean = false,
) {
    val color = if (highlightOrange) MaterialTheme.colorScheme.thermalOrange else MaterialTheme.colorScheme.memoryBlue
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        if (points.isEmpty()) return@Canvas
        val max = points.maxOf { it.value }.coerceAtLeast(1f)
        val min = points.minOf { it.value }
        val range = (max - min).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (points.size - 1).coerceAtLeast(1)
        val coordinates = points.mapIndexed { index, point ->
            val x = stepX * index
            val y = size.height - ((point.value - min) / range) * size.height
            Offset(x, y)
        }

        for (i in 0 until coordinates.lastIndex) {
            drawLine(
                color = color,
                start = coordinates[i],
                end = coordinates[i + 1],
                strokeWidth = 6f,
                cap = StrokeCap.Round,
            )
        }
        coordinates.forEach { point ->
            drawCircle(color = color, radius = 8f, center = point)
        }
    }
}

@Composable
fun TmBarChart(
    items: List<BarGroup>,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.memoryBlue
    val secondaryColor = MaterialTheme.colorScheme.networkGreen
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
    ) {
        if (items.isEmpty()) return@Canvas
        val max = items.maxOf { maxOf(it.primaryValue, it.secondaryValue) }.coerceAtLeast(1f)
        val groupWidth = size.width / items.size
        val barWidth = groupWidth * 0.28f

        items.forEachIndexed { index, item ->
            val startX = index * groupWidth + groupWidth * 0.22f
            val primaryHeight = size.height * (item.primaryValue / max)
            val secondaryHeight = size.height * (item.secondaryValue / max)

            drawRect(
                color = primaryColor,
                topLeft = Offset(startX, size.height - primaryHeight),
                size = Size(barWidth, primaryHeight),
            )
            drawRect(
                color = secondaryColor,
                topLeft = Offset(startX + barWidth + groupWidth * 0.08f, size.height - secondaryHeight),
                size = Size(barWidth, secondaryHeight),
            )
        }
    }
}

@Composable
fun TmDonutChart(
    slices: List<UsageSlice>,
    modifier: Modifier = Modifier,
) {
    val colors = listOf(
        MaterialTheme.colorScheme.networkGreen,
        MaterialTheme.colorScheme.memoryBlue,
        MaterialTheme.colorScheme.storagePurple,
        MaterialTheme.colorScheme.thermalOrange,
    )
    Canvas(
        modifier = modifier
            .height(160.dp)
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        if (slices.isEmpty()) return@Canvas
        val strokeWidth = size.minDimension * 0.16f
        var startAngle = -90f
        val diameter = size.minDimension
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        slices.forEachIndexed { index, slice ->
            val sweepAngle = slice.value * 360f
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth),
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun TmChartLabels(labels: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
