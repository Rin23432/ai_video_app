
package com.animegen.app.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animegen.app.data.local.AppPreferences
import com.animegen.app.data.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "",
    val deviceId: String = "",
    val saving: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

class SettingsViewModel(
    private val preferences: AppPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(preferences.baseUrlFlow, preferences.deviceIdFlow) { baseUrl, deviceId ->
                baseUrl to (deviceId ?: "")
            }.collect { pair ->
                _uiState.update {
                    it.copy(baseUrl = pair.first, deviceId = pair.second)
                }
            }
        }
    }

    fun onBaseUrlChange(value: String) {
        _uiState.update { it.copy(baseUrl = value) }
    }

    fun onDeviceIdChange(value: String) {
        _uiState.update { it.copy(deviceId = value) }
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, message = null, errorMessage = null) }
            try {
                preferences.setBaseUrl(state.baseUrl.trim())
                preferences.setDeviceId(state.deviceId.trim())
                authRepository.clearToken()
                _uiState.update { it.copy(saving = false, message = "已保存，已清理 token；下次请求会自动重新登录") }
            } catch (e: Exception) {
                _uiState.update { it.copy(saving = false, errorMessage = e.message ?: "保存失败") }
            }
        }
    }
}

