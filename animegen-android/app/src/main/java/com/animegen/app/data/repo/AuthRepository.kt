package com.animegen.app.data.repo

import com.animegen.app.data.local.AppPreferences
import com.animegen.app.data.network.ApiService
import com.animegen.app.data.network.AuthTokenResponse
import com.animegen.app.data.network.GuestTokenRequest
import com.animegen.app.data.network.GuestTokenResponse
import com.animegen.app.data.network.LoginRequest
import com.animegen.app.data.network.MeProfile
import com.animegen.app.data.network.RegisterRequest
import com.animegen.app.data.network.UpdateProfileRequest
import com.animegen.app.data.network.safeApiCall
import kotlinx.coroutines.flow.first
import java.util.UUID

class AuthRepository(
    private val apiService: ApiService,
    private val preferences: AppPreferences,
    private val onLoginRequired: () -> Unit,
    private val onAuthChanged: () -> Unit
) {
    suspend fun ensureGuestToken(): AppResult<String> {
        val token = preferences.tokenFlow.first()
        if (!token.isNullOrBlank()) return AppResult.Success(token)
        return refreshGuestToken()
    }

    suspend fun refreshGuestToken(): AppResult<String> {
        val deviceId = preferences.deviceIdFlow.first().orEmpty().ifBlank {
            UUID.randomUUID().toString().also { preferences.setDeviceId(it) }
        }
        return when (val result = safeApiCall { apiService.guestToken(GuestTokenRequest(deviceId)) }) {
            is AppResult.Success<GuestTokenResponse> -> {
                persistTokenAndUser(result.data.token, result.data.user.userId, result.data.user.nickname, result.data.user.role)
                AppResult.Success(result.data.token)
            }
            is AppResult.Failure -> handleFailure(result)
        }
    }

    suspend fun login(username: String, password: String): AppResult<MeProfile> {
        val result = safeApiCall { apiService.login(LoginRequest(username.trim(), password)) }
        return when (result) {
            is AppResult.Success<AuthTokenResponse> -> {
                persistTokenAndUser(
                    result.data.token,
                    result.data.user.userId,
                    result.data.user.nickname,
                    result.data.user.role,
                    clearTaskCache = true
                )
                me()
            }
            is AppResult.Failure -> handleFailure(result)
        }
    }

    suspend fun register(username: String, password: String, nickname: String): AppResult<MeProfile> {
        val result = safeApiCall { apiService.register(RegisterRequest(username.trim(), password, nickname.trim())) }
        return when (result) {
            is AppResult.Success<AuthTokenResponse> -> {
                persistTokenAndUser(
                    result.data.token,
                    result.data.user.userId,
                    result.data.user.nickname,
                    result.data.user.role,
                    clearTaskCache = true
                )
                me()
            }
            is AppResult.Failure -> handleFailure(result)
        }
    }

    suspend fun me(): AppResult<MeProfile> {
        return handleFailure(safeApiCall { apiService.me() })
    }

    suspend fun updateProfile(nickname: String, bio: String, avatarUrl: String): AppResult<MeProfile> {
        val request = UpdateProfileRequest(
            nickname = nickname.trim(),
            bio = bio.trim().ifBlank { null },
            avatarUrl = avatarUrl.trim().ifBlank { null }
        )
        return when (val result = safeApiCall { apiService.updateProfile(request) }) {
            is AppResult.Success<MeProfile> -> {
                preferences.setUserInfo(result.data.userId, result.data.nickname, result.data.role)
                AppResult.Success(result.data)
            }
            is AppResult.Failure -> handleFailure(result)
        }
    }

    suspend fun clearToken() {
        preferences.setToken(null)
        preferences.setUserInfo(null, null, null)
        preferences.setLastTask(null, null)
        onAuthChanged()
    }

    private suspend fun persistTokenAndUser(
        token: String,
        userId: Long,
        nickname: String,
        role: String,
        clearTaskCache: Boolean = false
    ) {
        preferences.setToken(token)
        preferences.setUserInfo(userId, nickname, role)
        if (clearTaskCache) {
            preferences.setLastTask(null, null)
        }
        onAuthChanged()
    }

    private fun <T> handleFailure(result: AppResult<T>): AppResult<T> {
        if (result is AppResult.Failure && (result.error is AppError.LoginRequired || result.error is AppError.Unauthorized)) {
            onLoginRequired()
        }
        return result
    }
}
