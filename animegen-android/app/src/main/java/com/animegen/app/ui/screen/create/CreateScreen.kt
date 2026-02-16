package com.animegen.app.ui.screen.create

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
        onApiKeyChange = vm::onApiKeyChange,
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
    onApiKeyChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onDismissError: () -> Unit
) {
    val errorMessage = state.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.create_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.create_subtitle),
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
                OutlinedTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.create_field_title)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.prompt,
                    onValueChange = onPromptChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.create_field_prompt)) },
                    minLines = 5
                )
                OutlinedTextField(
                    value = state.apiKey,
                    onValueChange = onApiKeyChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.create_field_api_key)) },
                    singleLine = true
                )
                Button(
                    onClick = onSubmit,
                    enabled = !state.isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.create_submit))
                }
                if (state.isSubmitting) {
                    CircularProgressIndicator()
                }
            }
        }

        if (errorMessage != null) {
            ErrorNotice(message = errorMessage, onRetry = onRetry)
            Button(onClick = onDismissError, shape = RoundedCornerShape(16.dp)) {
                Text(stringResource(R.string.create_dismiss))
            }
        }
    }
}




