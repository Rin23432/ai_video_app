package com.animegen.app.ui.screen.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animegen.app.AppContainer
import com.animegen.app.R
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
    val progress = state.progress.coerceIn(0, 100)
    val errorMessage = state.errorMessage
    val statusText = when (state.status.uppercase()) {
        "PENDING" -> stringResource(R.string.status_pending)
        "RUNNING" -> stringResource(R.string.status_running)
        "SUCCESS" -> stringResource(R.string.status_success)
        "FAIL", "FAILED" -> stringResource(R.string.status_fail)
        else -> state.status
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.task_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.task_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.initializing) {
                    Text(stringResource(R.string.task_loading))
                } else {
                    Text(stringResource(R.string.task_id_format, (state.taskId ?: "-").toString()))
                    Text(stringResource(R.string.task_status_format, statusText))
                    Text(stringResource(R.string.task_stage_format, state.stage))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("$progress%")
                    if (!state.isPolling) {
                        Button(
                            onClick = onRetry,
                            enabled = state.taskId != null,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(stringResource(R.string.task_retry_polling))
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            ErrorNotice(message = errorMessage, onRetry = onRetry)
        }
    }
}




