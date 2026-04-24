package com.flowerwine.taskmanager.data.system

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

data class AppUsageRecord(
    val packageName: String,
    val totalForegroundTimeMillis: Long,
    val lastTimeUsed: Long,
    val foregroundCount: Int,
)

class UsageStatsDataSource(
    context: Context,
    private val permissionStatusDataSource: PermissionStatusDataSource,
) {

    private val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)

    fun queryUsageRecords(startTime: Long, endTime: Long): List<AppUsageRecord> {
        if (!permissionStatusDataSource.hasUsageAccess()) return emptyList()
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime,
        )
        val eventCounts = queryForegroundEvents(startTime, endTime)

        return usageStats
            .asSequence()
            .filter { it.totalTimeInForeground > 0L || it.lastTimeUsed > 0L }
            .map { stats ->
                AppUsageRecord(
                    packageName = stats.packageName,
                    totalForegroundTimeMillis = stats.totalTimeInForeground,
                    lastTimeUsed = stats.lastTimeUsed,
                    foregroundCount = eventCounts[stats.packageName] ?: 0,
                )
            }
            .sortedByDescending { it.totalForegroundTimeMillis }
            .toList()
    }

    private fun queryForegroundEvents(startTime: Long, endTime: Long): Map<String, Int> {
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val result = mutableMapOf<String, Int>()
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                val packageName = event.packageName ?: continue
                result[packageName] = (result[packageName] ?: 0) + 1
            }
        }
        return result
    }
}
