package com.flowerwine.taskmanager.data.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.flowerwine.taskmanager.core.model.ToolActionType

class SettingsNavigator(private val context: Context) {

    fun open(actionType: ToolActionType, packageName: String = context.packageName) {
        val intent = when (actionType) {
            ToolActionType.OpenUsageAccess -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            ToolActionType.OpenOverlaySettings -> Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            )
            ToolActionType.OpenAppNotificationSettings -> Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            ToolActionType.OpenAppDetails -> Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:$packageName"),
            )
            ToolActionType.OpenDataUsageSettings -> Intent(Settings.ACTION_DATA_USAGE_SETTINGS)
            ToolActionType.OpenStorageSettings -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
                } else {
                    Intent(Settings.ACTION_MEMORY_CARD_SETTINGS)
                }
            }
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
