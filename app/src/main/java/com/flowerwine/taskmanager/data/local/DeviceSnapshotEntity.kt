package com.flowerwine.taskmanager.data.local

data class DeviceSnapshotEntity(
    val capturedAt: Long,
    val availableMemoryBytes: Long,
    val totalMemoryBytes: Long,
    val batteryLevelPercent: Int,
    val batteryTemperatureCelsius: Float?,
    val thermalStatus: Int,
    val storageFreeBytes: Long,
    val storageTotalBytes: Long,
    val wifiBytes: Long,
    val mobileBytes: Long,
)
