package com.animegen.app.ui.screen.auth

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animegen.app.AppContainer
import com.animegen.app.R
import com.animegen.app.data.repo.AppResult
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val nickname: String = "",
    val loading: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsernameChange(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun onNicknameChange(value: String) {
        _uiState.update { it.copy(nickname = value) }
    }

    fun login(onSuccess: () -> Unit) {
        val snapshot = _uiState.value
        if (snapshot.username.isBlank() || snapshot.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "请输入用户名和密码") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = container.authRepository.login(snapshot.username, snapshot.password)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(loading = false) }
                    onSuccess()
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(loading = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }

    fun register(onSuccess: () -> Unit) {
        val snapshot = _uiState.value
        if (snapshot.username.isBlank() || snapshot.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "注册需要用户名且密码至少6位") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (
                val result = container.authRepository.register(
                    snapshot.username,
                    snapshot.password,
                    snapshot.nickname.ifBlank { snapshot.username }
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(loading = false) }
                    onSuccess()
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(loading = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }
}

@Composable
fun LoginRoute(container: AppContainer, onSuccess: () -> Unit) {
    val vm: LoginViewModel = viewModel(factory = viewModelFactory { LoginViewModel(container) })
    val state by vm.uiState.collectAsState()
    LoginScreen(
        state = state,
        onUsernameChange = vm::onUsernameChange,
        onPasswordChange = vm::onPasswordChange,
        onNicknameChange = vm::onNicknameChange,
        onLogin = { vm.login(onSuccess) },
        onRegister = { vm.register(onSuccess) }
    )
}

@Composable
private fun LoginScreen(
    state: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onNicknameChange: (String) -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.login_title))
        OutlinedTextField(
            value = state.username,
            onValueChange = onUsernameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_username_label)) }
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_password_label)) }
        )
        OutlinedTextField(
            value = state.nickname,
            onValueChange = onNicknameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.login_nickname_label)) }
        )
        Button(onClick = onLogin, enabled = !state.loading) { Text(stringResource(R.string.login_button)) }
        Button(onClick = onRegister, enabled = !state.loading) { Text(stringResource(R.string.login_register_button)) }
        if (state.loading) CircularProgressIndicator()
        if (state.errorMessage != null) ErrorNotice(message = state.errorMessage, onRetry = onLogin)
    }
}





