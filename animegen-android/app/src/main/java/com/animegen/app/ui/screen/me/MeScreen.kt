package com.animegen.app.ui.screen.me

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import com.animegen.app.data.network.MeProfile
import com.animegen.app.data.repo.AppResult
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MeUiState(
    val loading: Boolean = false,
    val profile: MeProfile? = null,
    val errorMessage: String? = null
)

class MeViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(MeUiState())
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = container.authRepository.me()) {
                is AppResult.Success -> _uiState.update { it.copy(loading = false, profile = result.data) }
                is AppResult.Failure -> _uiState.update {
                    it.copy(loading = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            container.authRepository.clearToken()
            container.authRepository.refreshGuestToken()
            onDone()
        }
    }
}

@Composable
fun MeRoute(
    container: AppContainer,
    onOpenFavorites: () -> Unit,
    onOpenPublished: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLogin: () -> Unit
) {
    val vm: MeViewModel = viewModel(factory = viewModelFactory { MeViewModel(container) })
    val state by vm.uiState.collectAsState()
    val sessionState by container.sessionStateFlow.collectAsState()

    LaunchedEffect(sessionState) { vm.load() }

    MeScreen(
        state = state,
        onRetry = vm::load,
        onOpenFavorites = onOpenFavorites,
        onOpenPublished = onOpenPublished,
        onOpenProfile = onOpenProfile,
        onOpenSettings = onOpenSettings,
        onOpenLogin = onOpenLogin,
        onLogout = { vm.logout(onOpenLogin) }
    )
}

@Composable
private fun MeScreen(
    state: MeUiState,
    onRetry: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenPublished: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.me_title))
        if (state.loading) CircularProgressIndicator()
        if (state.errorMessage != null) ErrorNotice(message = state.errorMessage, onRetry = onRetry)

        val profile = state.profile
        if (profile != null) {
            val isGuest = profile.role.equals("GUEST", ignoreCase = true)
            val roleText = when (profile.role.uppercase()) {
                "GUEST" -> stringResource(R.string.me_role_guest)
                "USER" -> stringResource(R.string.me_role_user)
                "ADMIN" -> stringResource(R.string.me_role_admin)
                else -> profile.role
            }
            Text(stringResource(R.string.me_nickname_format, profile.nickname))
            Text(stringResource(R.string.me_role_format, roleText))
            Text(stringResource(R.string.me_bio_format, profile.bio ?: stringResource(R.string.me_bio_default)))
            Text(stringResource(R.string.me_published_format, profile.stats.published))
            Text(stringResource(R.string.me_favorites_format, profile.stats.favorites))
            Text(stringResource(R.string.me_likes_received_format, profile.stats.likesReceived))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onOpenPublished) { Text(stringResource(R.string.me_my_published)) }
                Button(onClick = onOpenFavorites) { Text(stringResource(R.string.me_my_favorites)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onOpenProfile, enabled = !isGuest) { Text(stringResource(R.string.me_edit_profile)) }
                OutlinedButton(onClick = onOpenSettings) { Text(stringResource(R.string.me_settings)) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                if (isGuest) {
                    Button(onClick = onOpenLogin) { Text(stringResource(R.string.me_login)) }
                } else {
                    OutlinedButton(onClick = onLogout) { Text(stringResource(R.string.me_logout_to_guest)) }
                }
            }
        }
    }
}




