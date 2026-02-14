package com.animegen.app.ui.screen.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
fun TaskRoute(
    container: AppContainer,
    taskIdArg: String?,
    workIdArg: String?,
    onNavigateDetail: (Long) -> Unit
) {
    val vm: TaskViewModel = viewModel(factory = viewModelFactory {
        TaskViewModel(container.taskRepository)
    })
    val state by vm.uiState.collectAsState()

    LaunchedEffect(taskIdArg, workIdArg) {
        vm.bind(taskIdArg?.toLongOrNull(), workIdArg?.toLongOrNull())
    }
    DisposableEffect(Unit) {
        onDispose { vm.stopPolling() }
    }

    LaunchedEffect(state.completedWorkId) {
        state.completedWorkId?.let {
            onNavigateDetail(it)
            vm.consumeNavigate()
        }
    }

    TaskScreen(state = state, onRetry = vm::retry)
}

@Composable
private fun TaskScreen(state: TaskUiState, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Task")
        if (state.initializing) {
            Text("加载中...")
        } else {
            Text("taskId: ${state.taskId ?: "-"}")
            Text("status: ${state.status}")
            Text("stage: ${state.stage}")
            LinearProgressIndicator(
                progress = { (state.progress.coerceIn(0, 100) / 100f) },
                modifier = Modifier.fillMaxWidth()
            )
            Text("${state.progress}%")
            if (!state.isPolling) {
                Button(onClick = onRetry, enabled = state.taskId != null) {
                    Text("重试轮询")
                }
            }
            if (state.errorMessage != null) {
                ErrorNotice(message = state.errorMessage, onRetry = onRetry)
            }
        }
    }
}

