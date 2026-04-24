package com.flowerwine.taskmanager.core.di

import android.content.Context
import com.flowerwine.taskmanager.data.local.SnapshotStore
import com.flowerwine.taskmanager.data.preferences.UserPreferencesRepository
import com.flowerwine.taskmanager.data.repository.AppsRepository
import com.flowerwine.taskmanager.data.repository.DeviceRepository
import com.flowerwine.taskmanager.data.repository.ToolsRepository
import com.flowerwine.taskmanager.data.system.DeviceStatusDataSource
import com.flowerwine.taskmanager.data.system.InstalledAppsDataSource
import com.flowerwine.taskmanager.data.system.PermissionStatusDataSource
import com.flowerwine.taskmanager.data.system.SettingsNavigator
import com.flowerwine.taskmanager.data.system.UsageStatsDataSource
import com.flowerwine.taskmanager.domain.usecase.BuildToolRecommendationsUseCase
import com.flowerwine.taskmanager.feature.analysis.AnalysisViewModel
import com.flowerwine.taskmanager.feature.apps.AppsViewModel
import com.flowerwine.taskmanager.feature.overview.OverviewViewModel
import com.flowerwine.taskmanager.feature.tools.ToolsViewModel

class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val snapshotStore = SnapshotStore(appContext)
    private val preferencesRepository = UserPreferencesRepository(appContext)
    private val permissionStatusDataSource = PermissionStatusDataSource(appContext)
    private val deviceStatusDataSource = DeviceStatusDataSource(appContext)
    private val usageStatsDataSource = UsageStatsDataSource(appContext, permissionStatusDataSource)
    private val installedAppsDataSource = InstalledAppsDataSource(appContext)

    val settingsNavigator = SettingsNavigator(appContext)
    val deviceRepository = DeviceRepository(
        snapshotStore = snapshotStore,
        deviceStatusDataSource = deviceStatusDataSource,
        usageStatsDataSource = usageStatsDataSource,
        permissionStatusDataSource = permissionStatusDataSource,
        preferencesRepository = preferencesRepository,
    )
    val appsRepository = AppsRepository(
        installedAppsDataSource = installedAppsDataSource,
        usageStatsDataSource = usageStatsDataSource,
        deviceStatusDataSource = deviceStatusDataSource,
        permissionStatusDataSource = permissionStatusDataSource,
        preferencesRepository = preferencesRepository,
    )
    private val buildToolRecommendationsUseCase = BuildToolRecommendationsUseCase()
    val toolsRepository = ToolsRepository(
        permissionStatusDataSource = permissionStatusDataSource,
        deviceStatusDataSource = deviceStatusDataSource,
        appsRepository = appsRepository,
        settingsNavigator = settingsNavigator,
        buildToolRecommendationsUseCase = buildToolRecommendationsUseCase,
    )

    fun createOverviewViewModel(): OverviewViewModel = OverviewViewModel(deviceRepository)

    fun createAnalysisViewModel(): AnalysisViewModel = AnalysisViewModel(deviceRepository)

    fun createAppsViewModel(): AppsViewModel = AppsViewModel(appsRepository)

    fun createToolsViewModel(): ToolsViewModel = ToolsViewModel(toolsRepository)
}
