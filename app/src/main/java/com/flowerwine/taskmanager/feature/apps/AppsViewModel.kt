package com.flowerwine.taskmanager.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowerwine.taskmanager.core.model.AppSortOption
import com.flowerwine.taskmanager.core.model.AppsDashboard
import com.flowerwine.taskmanager.data.repository.AppsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AppsUiState(
    val query: String = "",
    val isLoading: Boolean = true,
    val dashboard: AppsDashboard? = null,
)

class AppsViewModel(
    private val appsRepository: AppsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                appsRepository.selectedSortOption,
                appsRepository.includeSystemApps,
            ) { sortOption, includeSystemApps ->
                sortOption to includeSystemApps
            }.collectLatest { (sortOption, includeSystemApps) ->
                load(sortOption, _uiState.value.query, includeSystemApps)
            }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        viewModelScope.launch {
            load(
                _uiState.value.dashboard?.sortOption ?: AppSortOption.MostUsed,
                query,
                _uiState.value.dashboard?.includeSystemApps ?: false,
            )
        }
    }

    fun onSortSelected(sortOption: AppSortOption) {
        viewModelScope.launch {
            appsRepository.setSortOption(sortOption)
        }
    }

    fun onIncludeSystemAppsChanged(includeSystemApps: Boolean) {
        viewModelScope.launch {
            appsRepository.setIncludeSystemApps(includeSystemApps)
        }
    }

    private suspend fun load(sortOption: AppSortOption, query: String, includeSystemApps: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        _uiState.value = AppsUiState(
            query = query,
            isLoading = false,
            dashboard = appsRepository.getDashboard(sortOption, query, includeSystemApps),
        )
    }
}
