package com.animegen.app.data.repo

import com.animegen.app.data.local.AppPreferences
import com.animegen.app.data.network.ApiService
import com.animegen.app.data.network.CreateWorkRequest
import com.animegen.app.data.network.CreateWorkResponse
import com.animegen.app.data.network.Work
import com.animegen.app.data.network.safeApiCall

class WorksRepository(
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val preferences: AppPreferences
) {
    suspend fun createWork(request: CreateWorkRequest): AppResult<CreateWorkResponse> {
        return authorizedCall { safeApiCall { apiService.createWork(request) } }
    }

    suspend fun listWorks(cursor: Long = 0, limit: Int = 20): AppResult<List<Work>> {
        return authorizedCall { safeApiCall { apiService.listWorks(cursor, limit) } }
    }

    suspend fun getWork(workId: Long): AppResult<Work> {
        return authorizedCall { safeApiCall { apiService.getWork(workId) } }
    }

    suspend fun saveLastTask(taskId: Long, workId: Long) {
        preferences.setLastTask(taskId, workId)
    }

    private suspend fun <T> authorizedCall(block: suspend () -> AppResult<T>): AppResult<T> {
        val auth = authRepository.ensureGuestToken()
        if (auth is AppResult.Failure) return auth
        val firstTry = block()
        if (firstTry is AppResult.Failure && firstTry.error is AppError.Unauthorized) {
            authRepository.clearToken()
            val refresh = authRepository.ensureGuestToken()
            if (refresh is AppResult.Failure) return refresh
            return block()
        }
        return firstTry
    }
}

