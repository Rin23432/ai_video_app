package com.animegen.app

import android.content.Context
import com.animegen.app.data.local.AppPreferences
import com.animegen.app.data.network.ApiResponse
import com.animegen.app.data.network.ApiService
import com.animegen.app.data.network.CreateWorkRequest
import com.animegen.app.data.network.CreateWorkResponse
import com.animegen.app.data.network.GuestTokenRequest
import com.animegen.app.data.network.GuestTokenResponse
import com.animegen.app.data.network.NetworkClient
import com.animegen.app.data.network.OfflineMockApiService
import com.animegen.app.data.network.TaskStatusResponse
import com.animegen.app.data.network.Work
import com.animegen.app.data.repo.AuthRepository
import com.animegen.app.data.repo.TaskRepository
import com.animegen.app.data.repo.WorksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var currentToken: String? = null

    @Volatile
    private var currentBaseUrl: String = BuildConfig.API_BASE_URL

    val preferences = AppPreferences(context, BuildConfig.API_BASE_URL)

    private val offlineMockApiService = OfflineMockApiService()

    private val networkClient = NetworkClient(
        baseUrlProvider = { currentBaseUrl },
        tokenProvider = { currentToken }
    )

    private val apiService = object : ApiService {
        private fun activeService(): ApiService {
            return if (currentBaseUrl.trim().equals("mock://offline", ignoreCase = true)) {
                offlineMockApiService
            } else {
                networkClient.apiService
            }
        }

        override suspend fun guestToken(request: GuestTokenRequest): ApiResponse<GuestTokenResponse> =
            activeService().guestToken(request)

        override suspend fun createWork(request: CreateWorkRequest): ApiResponse<CreateWorkResponse> =
            activeService().createWork(request)

        override suspend fun getTask(taskId: Long): ApiResponse<TaskStatusResponse> =
            activeService().getTask(taskId)

        override suspend fun listWorks(cursor: Long, limit: Int): ApiResponse<List<Work>> =
            activeService().listWorks(cursor, limit)

        override suspend fun getWork(workId: Long): ApiResponse<Work> =
            activeService().getWork(workId)
    }

    val authRepository = AuthRepository(apiService, preferences)
    val worksRepository = WorksRepository(apiService, authRepository, preferences)
    val taskRepository = TaskRepository(apiService, authRepository, preferences)

    init {
        scope.launch {
            preferences.tokenFlow.collectLatest { currentToken = it }
        }
        scope.launch {
            preferences.baseUrlFlow.collectLatest { currentBaseUrl = it }
        }
    }
}

