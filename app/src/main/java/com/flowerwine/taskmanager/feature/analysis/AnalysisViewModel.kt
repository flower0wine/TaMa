package com.flowerwine.taskmanager.feature.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowerwine.taskmanager.core.model.AnalysisDashboard
import com.flowerwine.taskmanager.core.model.AnalysisRange
import com.flowerwine.taskmanager.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val dashboard: AnalysisDashboard? = null,
)

class AnalysisViewModel(
    private val deviceRepository: DeviceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deviceRepository.selectedAnalysisRange.collectLatest { range ->
                load(range)
            }
        }
    }

    fun onRangeSelected(range: AnalysisRange) {
        viewModelScope.launch {
            deviceRepository.setAnalysisRange(range)
        }
    }

    private suspend fun load(range: AnalysisRange) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        _uiState.value = AnalysisUiState(
            isLoading = false,
            dashboard = deviceRepository.getAnalysisDashboard(range),
        )
    }
}
