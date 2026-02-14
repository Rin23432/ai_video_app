package com.animegen.app.ui.screen.community

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animegen.app.AppContainer
import com.animegen.app.data.repo.AppResult
import com.animegen.app.data.repo.CommunityRepository
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityPublishUiState(
    val title: String = "",
    val description: String = "",
    val posting: Boolean = false,
    val errorMessage: String? = null
)

class CommunityPublishViewModel(
    private val repository: CommunityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityPublishUiState())
    val uiState: StateFlow<CommunityPublishUiState> = _uiState.asStateFlow()

    fun setTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun setDescription(value: String) = _uiState.update { it.copy(description = value) }

    fun publish(workId: Long, onDone: (Long) -> Unit) {
        val title = _uiState.value.title.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "title is required") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(posting = true, errorMessage = null) }
            when (val result = repository.publish(workId, title, _uiState.value.description.trim().ifBlank { null })) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(posting = false) }
                    onDone(result.data.contentId)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(posting = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }
}

@Composable
fun CommunityPublishRoute(container: AppContainer, workIdArg: String?, onDone: (Long) -> Unit) {
    val vm: CommunityPublishViewModel = viewModel(factory = viewModelFactory {
        CommunityPublishViewModel(container.communityRepository)
    })
    val state by vm.uiState.collectAsState()
    val workId = workIdArg?.toLongOrNull()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Publish To Community")
        Text("WorkId: ${workId ?: "invalid"}")
        OutlinedTextField(
            value = state.title,
            onValueChange = vm::setTitle,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title") }
        )
        OutlinedTextField(
            value = state.description,
            onValueChange = vm::setDescription,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description") }
        )
        if (state.errorMessage != null) {
            ErrorNotice(message = state.errorMessage, onRetry = {})
        }
        Button(
            onClick = { if (workId != null) vm.publish(workId, onDone) },
            enabled = !state.posting && workId != null
        ) {
            if (state.posting) CircularProgressIndicator()
            else Text("Publish")
        }
    }
}
