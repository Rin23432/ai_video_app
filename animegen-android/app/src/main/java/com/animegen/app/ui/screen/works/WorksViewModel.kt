package com.animegen.app.ui.screen.works

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animegen.app.data.network.Work
import com.animegen.app.data.repo.AppResult
import com.animegen.app.data.repo.WorksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorksUiState(
    val loading: Boolean = false,
    val works: List<Work> = emptyList(),
    val errorMessage: String? = null
)

class WorksViewModel(
    private val worksRepository: WorksRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorksUiState())
    val uiState: StateFlow<WorksUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = worksRepository.listWorks()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(loading = false, works = result.data)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(loading = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }
}

