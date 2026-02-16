package com.animegen.app.ui.screen.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animegen.app.data.repo.AppError
import com.animegen.app.data.repo.AppResult
import com.animegen.app.data.repo.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskUiState(
    val initializing: Boolean = true,
    val taskId: Long? = null,
    val workId: Long? = null,
    val status: String = "PENDING",
    val progress: Int = 0,
    val stage: String = "等待中",
    val errorMessage: String? = null,
    val isPolling: Boolean = false,
    val completedWorkId: Long? = null
)

class TaskViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private var pollJob: Job? = null

    fun bind(taskIdArg: Long?, workIdArg: Long?) {
        viewModelScope.launch {
            _uiState.update { it.copy(initializing = true) }
            val (lastTaskId, lastWorkId) = taskRepository.getLastTask()
            val taskId = taskIdArg ?: lastTaskId
            val workId = workIdArg ?: lastWorkId
            _uiState.update { it.copy(taskId = taskId, workId = workId, initializing = false) }
            if (taskId != null) startPolling() else _uiState.update { it.copy(errorMessage = "暂无任务，请先在创作页发起任务") }
        }
    }

    fun startPolling() {
        val taskId = _uiState.value.taskId ?: return
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            _uiState.update { it.copy(isPolling = true, errorMessage = null) }
            while (true) {
                when (val result = taskRepository.getTask(taskId)) {
                    is AppResult.Success -> {
                        val data = result.data
                        _uiState.update {
                            it.copy(
                                status = data.status,
                                progress = data.progress ?: 0,
                                stage = data.stage ?: "处理中"
                            )
                        }
                        if (data.status == "SUCCESS") {
                            val workId = _uiState.value.workId
                            _uiState.update {
                                it.copy(
                                    isPolling = false,
                                    completedWorkId = workId,
                                    errorMessage = null
                                )
                            }
                            break
                        }
                        if (data.status == "FAIL") {
                            val msg = data.errorMessage ?: "任务失败"
                            _uiState.update {
                                it.copy(isPolling = false, errorMessage = AppError.TaskFailed(msg).displayMessage)
                            }
                            break
                        }
                    }
                    is AppResult.Failure -> {
                        _uiState.update { it.copy(isPolling = false, errorMessage = result.error.displayMessage) }
                        break
                    }
                }
                delay(1500)
            }
        }
    }

    fun retry() {
        if (_uiState.value.taskId == null) {
            bind(null, null)
        } else {
            startPolling()
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        _uiState.update { it.copy(isPolling = false) }
    }

    fun consumeNavigate() {
        _uiState.update { it.copy(completedWorkId = null) }
    }

    override fun onCleared() {
        stopPolling()
        super.onCleared()
    }
}


