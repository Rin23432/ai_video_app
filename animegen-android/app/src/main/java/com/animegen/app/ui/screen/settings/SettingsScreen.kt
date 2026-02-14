package com.animegen.app.ui.screen.settings

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animegen.app.AppContainer
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory

@Composable
fun SettingsRoute(container: AppContainer) {
    val vm: SettingsViewModel = viewModel(factory = viewModelFactory {
        SettingsViewModel(container.preferences, container.authRepository)
    })
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings")
        OutlinedTextField(
            value = state.baseUrl,
            onValueChange = vm::onBaseUrlChange,
            label = { Text("baseUrl") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.deviceId,
            onValueChange = vm::onDeviceIdChange,
            label = { Text("deviceId") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = vm::save, enabled = !state.saving) {
            Text("保存")
        }
        if (state.saving) CircularProgressIndicator()
        if (state.message != null) Text(state.message)
        if (state.errorMessage != null) ErrorNotice(message = state.errorMessage, onRetry = vm::save)
    }
}

