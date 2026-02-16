package com.animegen.app.data.repo

import com.animegen.app.data.local.AppPreferences
import com.animegen.app.data.network.ApiService
import com.animegen.app.data.network.TaskStatusResponse
import com.animegen.app.data.network.safeApiCall
import kotlinx.coroutines.flow.first

class TaskRepository(
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val preferences: AppPreferences,
    private val onLoginRequired: () -> Unit
) {
    suspend fun getTask(taskId: Long): AppResult<TaskStatusResponse> {
        return authorizedCall { safeApiCall { apiService.getTask(taskId) } }
    }

    suspend fun getLastTask(): Pair<Long?, Long?> {
        return preferences.lastTaskIdFlow.first() to preferences.lastWorkIdFlow.first()
    }

    private suspend fun <T> authorizedCall(block: suspend () -> AppResult<T>): AppResult<T> {
        val auth = authRepository.ensureGuestToken()
        if (auth is AppResult.Failure) return auth
        val firstTry = block()
        if (firstTry is AppResult.Failure && (firstTry.error is AppError.LoginRequired || firstTry.error is AppError.Unauthorized)) {
            onLoginRequired()
        }
        if (firstTry is AppResult.Failure && firstTry.error is AppError.Unauthorized) {
            authRepository.clearToken()
            val refresh = authRepository.ensureGuestToken()
            if (refresh is AppResult.Failure) return refresh
            return block()
        }
        return firstTry
    }
}

