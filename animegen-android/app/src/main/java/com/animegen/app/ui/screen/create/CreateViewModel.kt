package com.animegen.app.ui.screen.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animegen.app.data.network.CreateWorkRequest
import com.animegen.app.data.repo.AppResult
import com.animegen.app.data.repo.WorksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CreateUiState(
    val title: String = "",
    val prompt: String = "",
    val requestId: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val createdTaskId: Long? = null,
    val createdWorkId: Long? = null
)

class CreateViewModel(
    private val worksRepository: WorksRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateUiState())
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, requestId = null) }
    }

    fun onPromptChange(value: String) {
        _uiState.update { it.copy(prompt = value, requestId = null) }
    }

    fun submit() {
        val state = _uiState.value
        if (state.title.isBlank() || state.prompt.isBlank()) {
            _uiState.update { it.copy(errorMessage = "标题和 Prompt 不能为空") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val requestId = state.requestId ?: UUID.randomUUID().toString()
            val req = CreateWorkRequest(
                requestId = requestId,
                title = state.title,
                prompt = state.prompt,
                styleId = "anime_shonen",
                aspectRatio = "9:16",
                durationSec = 30,
                mode = "CLOUD"
            )
            when (val result = worksRepository.createWork(req)) {
                is AppResult.Success -> {
                    worksRepository.saveLastTask(result.data.taskId, result.data.workId)
                    _uiState.update {
                        it.copy(
                            requestId = requestId,
                            isSubmitting = false,
                            createdTaskId = result.data.taskId,
                            createdWorkId = result.data.workId
                        )
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(requestId = requestId, isSubmitting = false, errorMessage = result.error.displayMessage)
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeNavigation() {
        _uiState.update { it.copy(createdTaskId = null, createdWorkId = null) }
    }
}

