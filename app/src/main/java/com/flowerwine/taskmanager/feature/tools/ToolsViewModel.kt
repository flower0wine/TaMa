package com.flowerwine.taskmanager.feature.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowerwine.taskmanager.core.model.ToolsDashboard
import com.flowerwine.taskmanager.data.repository.ToolsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ToolsUiState(
    val isLoading: Boolean = true,
    val dashboard: ToolsDashboard? = null,
)

class ToolsViewModel(
    private val toolsRepository: ToolsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToolsUiState())
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = ToolsUiState(
                isLoading = false,
                dashboard = toolsRepository.getDashboard(),
            )
        }
    }
}
