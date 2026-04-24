package com.flowerwine.taskmanager.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowerwine.taskmanager.core.model.OverviewDashboard
import com.flowerwine.taskmanager.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OverviewUiState(
    val isLoading: Boolean = true,
    val dashboard: OverviewDashboard? = null,
)

class OverviewViewModel(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = OverviewUiState(
                isLoading = false,
                dashboard = deviceRepository.getOverviewDashboard(),
            )
        }
    }
}
