package com.flowerwine.taskmanager.core.util

import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

private val oneDecimal = DecimalFormat("0.0")

fun formatBytes(bytes: Long): String {
    if (bytes < 0) return "待获取"
    val kb = 1024f
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        bytes >= gb -> "${oneDecimal.format(bytes / gb)} GB"
        bytes >= mb -> "${oneDecimal.format(bytes / mb)} MB"
        bytes >= kb -> "${oneDecimal.format(bytes / kb)} KB"
        else -> "$bytes B"
    }
}

fun formatUsageDuration(millis: Long): String {
    if (millis <= 0) return "0 分钟"
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    return when {
        hours > 0 -> "${hours}小时${minutes}分钟"
        else -> "${minutes.coerceAtLeast(1)} 分钟"
    }
}

fun formatRelativeMinutes(diffMillis: Long): String {
    if (diffMillis <= 0) return "刚刚"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis).coerceAtLeast(1)
    return "${minutes} 分钟前"
}

fun formatTemperature(celsius: Float?): String {
    return celsius?.let { "${oneDecimal.format(it)}°C" } ?: "待获取"
}

fun formatPercentage(value: Float): String = "${value.toInt()}%"

fun formatSignedTrend(value: Long): String {
    val sign = if (value > 0) "+" else if (value < 0) "-" else ""
    return "$sign${value.absoluteValue}"
}
