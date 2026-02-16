package com.animegen.app.ui.screen.me

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

data class ProfileUiState(
    val nickname: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val loading: Boolean = false,
    val saving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null, message = null) }
            when (val result = container.authRepository.me()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        loading = false,
                        nickname = result.data.nickname,
                        bio = result.data.bio ?: "",
                        avatarUrl = result.data.avatarUrl ?: ""
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(loading = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }

    fun onNicknameChange(value: String) {
        _uiState.update { it.copy(nickname = value) }
    }

    fun onBioChange(value: String) {
        _uiState.update { it.copy(bio = value) }
    }

    fun onAvatarChange(value: String) {
        _uiState.update { it.copy(avatarUrl = value) }
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, errorMessage = null, message = null) }
            when (val result = container.authRepository.updateProfile(state.nickname, state.bio, state.avatarUrl)) {
                is AppResult.Success -> _uiState.update { it.copy(saving = false, message = "保存成功") }
                is AppResult.Failure -> _uiState.update {
                    it.copy(saving = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }
}

@Composable
fun ProfileRoute(container: AppContainer) {
    val vm: ProfileViewModel = viewModel(factory = viewModelFactory { ProfileViewModel(container) })
    val state by vm.uiState.collectAsState()
    val sessionState by container.sessionStateFlow.collectAsState()

    LaunchedEffect(sessionState) { vm.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.profile_title))
        OutlinedTextField(
            value = state.nickname,
            onValueChange = vm::onNicknameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.profile_nickname_label)) }
        )
        OutlinedTextField(
            value = state.bio,
            onValueChange = vm::onBioChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.profile_bio_label)) }
        )
        OutlinedTextField(
            value = state.avatarUrl,
            onValueChange = vm::onAvatarChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.profile_avatar_url_label)) }
        )
        Button(onClick = vm::save, enabled = !state.saving) { Text(stringResource(R.string.profile_save_button)) }
        if (state.loading || state.saving) CircularProgressIndicator()
        val message = state.message
        if (message != null) Text(message)
        val errorMessage = state.errorMessage
        if (errorMessage != null) ErrorNotice(message = errorMessage, onRetry = vm::save)
    }
}


