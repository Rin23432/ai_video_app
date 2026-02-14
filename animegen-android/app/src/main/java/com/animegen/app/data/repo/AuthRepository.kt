package com.animegen.app.data.repo

import com.animegen.app.data.local.AppPreferences
import com.animegen.app.data.network.GuestTokenRequest
import com.animegen.app.data.network.ApiService
import com.animegen.app.data.network.GuestTokenResponse
import com.animegen.app.data.network.safeApiCall
import kotlinx.coroutines.flow.first
import java.util.UUID

class AuthRepository(
    private val apiService: ApiService,
    private val preferences: AppPreferences
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
                preferences.setToken(result.data.token)
                AppResult.Success(result.data.token)
            }
            is AppResult.Failure -> result
        }
    }

    suspend fun clearToken() {
        preferences.setToken(null)
    }
}

