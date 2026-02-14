package com.animegen.app.ui.screen.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animegen.app.AppContainer
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory

@Composable
fun CreateRoute(
    container: AppContainer,
    onCreated: (taskId: Long, workId: Long) -> Unit
) {
    val vm: CreateViewModel = viewModel(factory = viewModelFactory {
        CreateViewModel(container.worksRepository)
    })
    val state by vm.uiState.collectAsState()

    LaunchedEffect(state.createdTaskId, state.createdWorkId) {
        val taskId = state.createdTaskId
        val workId = state.createdWorkId
        if (taskId != null && workId != null) {
            onCreated(taskId, workId)
            vm.consumeNavigation()
        }
    }

    CreateScreen(
        state = state,
        onTitleChange = vm::onTitleChange,
        onPromptChange = vm::onPromptChange,
        onSubmit = vm::submit,
        onRetry = vm::submit,
        onDismissError = vm::clearError
    )
}

@Composable
private fun CreateScreen(
    state: CreateUiState,
    onTitleChange: (String) -> Unit,
    onPromptChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create")
        OutlinedTextField(
            value = state.title,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title") }
        )
        OutlinedTextField(
            value = state.prompt,
            onValueChange = onPromptChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Prompt") },
            minLines = 4
        )
        Button(onClick = onSubmit, enabled = !state.isSubmitting) {
            Text("POST /works")
        }
        if (state.isSubmitting) {
            CircularProgressIndicator()
        }
        if (state.errorMessage != null) {
            ErrorNotice(message = state.errorMessage, onRetry = onRetry)
            Button(onClick = onDismissError) { Text("关闭提示") }
        }
    }
}

