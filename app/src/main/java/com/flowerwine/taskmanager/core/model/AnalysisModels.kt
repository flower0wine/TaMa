package com.flowerwine.taskmanager.core.model

enum class AnalysisRange(val label: String) {
    Today("今日"),
    SevenDays("7天"),
    ThirtyDays("30天"),
}

data class ChartPoint(
    val label: String,
    val value: Float,
)

data class BarGroup(
    val label: String,
    val primaryValue: Float,
    val secondaryValue: Float,
)

data class UsageSlice(
    val label: String,
    val value: Float,
    val displayValue: String,
)

data class AnalysisDashboard(
    val selectedRange: AnalysisRange,
    val memoryTrend: List<ChartPoint>,
    val temperatureTrend: List<ChartPoint>,
    val networkTrend: List<BarGroup>,
    val usageDistribution: List<UsageSlice>,
    val memoryAverageLabel: String,
    val lowMemoryAlertsLabel: String,
    val peakTemperatureLabel: String,
    val insightTitle: String,
    val insightBody: String,
)
