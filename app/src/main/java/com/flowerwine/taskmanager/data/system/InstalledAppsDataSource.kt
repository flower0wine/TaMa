package com.flowerwine.taskmanager.data.system

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build

data class InstalledApp(
    val packageName: String,
    val displayName: String,
    val uid: Int,
    val isSystemApp: Boolean,
)

class InstalledAppsDataSource(private val context: Context) {

    private val packageManager = context.packageManager

    fun getLauncherApps(): List<InstalledApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }
        return activities
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                InstalledApp(
                    packageName = activityInfo.packageName,
                    displayName = resolveInfo.loadLabel(packageManager).toString(),
                    uid = activityInfo.applicationInfo.uid,
                    isSystemApp = activityInfo.applicationInfo.isSystemApp(),
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.displayName.lowercase() }
    }

    fun getInstalledApps(): List<InstalledApp> {
        val applications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(0)
        }

        return applications
            .asSequence()
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .map { applicationInfo ->
                InstalledApp(
                    packageName = applicationInfo.packageName,
                    displayName = packageManager.getApplicationLabel(applicationInfo).toString(),
                    uid = applicationInfo.uid,
                    isSystemApp = applicationInfo.isSystemApp(),
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.displayName.lowercase() }
            .toList()
    }

    fun getAppLabel(packageName: String): String {
        return runCatching {
            val applicationInfo = getApplicationInfo(packageName) ?: return@runCatching packageName.substringAfterLast('.')
            packageManager.getApplicationLabel(applicationInfo).toString()
        }.getOrDefault(packageName.substringAfterLast('.'))
    }

    fun getApplicationInfo(packageName: String): ApplicationInfo? {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
        }.getOrNull()
    }
}

private fun ApplicationInfo.isSystemApp(): Boolean {
    val systemFlags = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
    return (flags and systemFlags) != 0
}
