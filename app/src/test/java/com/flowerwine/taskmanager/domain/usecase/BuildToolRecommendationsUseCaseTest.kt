package com.flowerwine.taskmanager.domain.usecase

import com.flowerwine.taskmanager.core.model.PermissionStatus
import com.flowerwine.taskmanager.core.model.ToolActionType
import com.flowerwine.taskmanager.data.system.DeviceSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildToolRecommendationsUseCaseTest {

    private val useCase = BuildToolRecommendationsUseCase()

    @Test
    fun invoke_returnsUsageAccessRecommendation_whenPermissionMissing() {
        val result = useCase(
            permissionStatus = PermissionStatus(
                usageAccessGranted = false,
                notificationsGranted = true,
                overlayGranted = true,
            ),
            snapshot = snapshot(),
            heaviestApp = null,
        )

        assertTrue(result.any { it.actionType == ToolActionType.OpenUsageAccess })
    }

    @Test
    fun invoke_prioritizesHeatRecommendation_whenTemperatureHigh() {
        val result = useCase(
            permissionStatus = PermissionStatus(
                usageAccessGranted = true,
                notificationsGranted = true,
                overlayGranted = true,
            ),
            snapshot = snapshot(temperature = 41.2f),
            heaviestApp = "微信",
        )

        assertEquals("温度偏高", result.first().title)
    }

    private fun snapshot(temperature: Float? = 36.4f): DeviceSnapshot {
        return DeviceSnapshot(
            capturedAt = 0L,
            availableMemoryBytes = 3L,
            totalMemoryBytes = 8L,
            batteryLevelPercent = 68,
            batteryTemperatureCelsius = temperature,
            thermalStatus = 0,
            storageFreeBytes = 128L,
            storageTotalBytes = 256L,
            wifiBytes = 0L,
            mobileBytes = 0L,
        )
    }
}
