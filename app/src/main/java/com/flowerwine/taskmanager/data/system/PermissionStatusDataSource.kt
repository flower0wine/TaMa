package com.flowerwine.taskmanager.data.system

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.flowerwine.taskmanager.core.model.PermissionStatus

class PermissionStatusDataSource(private val context: Context) {

    fun getPermissionStatus(): PermissionStatus {
        return PermissionStatus(
            usageAccessGranted = hasUsageAccess(),
            notificationsGranted = hasNotificationAccess(),
            overlayGranted = hasOverlayAccess(),
        )
    }

    fun hasUsageAccess(): Boolean {
        val appOpsManager = context.getSystemService(AppOpsManager::class.java)
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasNotificationAccess(): Boolean {
        return context.getSystemService(NotificationManager::class.java).areNotificationsEnabled()
    }

    fun hasOverlayAccess(): Boolean = Settings.canDrawOverlays(context)
}
