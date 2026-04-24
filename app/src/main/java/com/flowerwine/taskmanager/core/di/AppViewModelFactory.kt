package com.flowerwine.taskmanager.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.flowerwine.taskmanager.feature.analysis.AnalysisViewModel
import com.flowerwine.taskmanager.feature.apps.AppsViewModel
import com.flowerwine.taskmanager.feature.overview.OverviewViewModel
import com.flowerwine.taskmanager.feature.tools.ToolsViewModel

class AppViewModelFactory(
    private val appContainer: AppContainer,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(OverviewViewModel::class.java) -> appContainer.createOverviewViewModel() as T
            modelClass.isAssignableFrom(AnalysisViewModel::class.java) -> appContainer.createAnalysisViewModel() as T
            modelClass.isAssignableFrom(AppsViewModel::class.java) -> appContainer.createAppsViewModel() as T
            modelClass.isAssignableFrom(ToolsViewModel::class.java) -> appContainer.createToolsViewModel() as T
            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
