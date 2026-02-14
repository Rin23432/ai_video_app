package com.animegen.app.ui.screen.workdetail

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

data class WorkDetailUiState(
    val loading: Boolean = false,
    val work: Work? = null,
    val errorMessage: String? = null
)

class WorkDetailViewModel(
    private val worksRepository: WorksRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkDetailUiState())
    val uiState: StateFlow<WorkDetailUiState> = _uiState.asStateFlow()

    fun load(workId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = worksRepository.getWork(workId)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(loading = false, work = result.data)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(loading = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }
}

