package com.animegen.app

import android.content.Context
import com.animegen.app.data.local.AppPreferences
import com.animegen.app.data.network.ApiResponse
import com.animegen.app.data.network.ApiService
import com.animegen.app.data.network.CreateWorkRequest
import com.animegen.app.data.network.CreateWorkResponse
import com.animegen.app.data.network.GuestTokenRequest
import com.animegen.app.data.network.GuestTokenResponse
import com.animegen.app.data.network.CommunityCommentListResponse
import com.animegen.app.data.network.CommunityContentDetail
import com.animegen.app.data.network.CommunityCreateCommentRequest
import com.animegen.app.data.network.CommunityCreateCommentResponse
import com.animegen.app.data.network.CommunityFeedResponse
import com.animegen.app.data.network.CommunityPublishRequest
import com.animegen.app.data.network.CommunityPublishResponse
import com.animegen.app.data.network.CommunityToggleRequest
import com.animegen.app.data.network.CommunityToggleResponse
import com.animegen.app.data.network.NetworkClient
import com.animegen.app.data.network.OfflineMockApiService
import com.animegen.app.data.network.TaskStatusResponse
import com.animegen.app.data.network.Work
import com.animegen.app.data.repo.AuthRepository
import com.animegen.app.data.repo.CommunityRepository
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

        override suspend fun publishCommunity(request: CommunityPublishRequest): ApiResponse<CommunityPublishResponse> =
            activeService().publishCommunity(request)

        override suspend fun listCommunityContents(tab: String, cursor: Long, limit: Int): ApiResponse<CommunityFeedResponse> =
            activeService().listCommunityContents(tab, cursor, limit)

        override suspend fun getCommunityDetail(contentId: Long): ApiResponse<CommunityContentDetail> =
            activeService().getCommunityDetail(contentId)

        override suspend fun toggleLike(contentId: Long, request: CommunityToggleRequest): ApiResponse<CommunityToggleResponse> =
            activeService().toggleLike(contentId, request)

        override suspend fun toggleFavorite(contentId: Long, request: CommunityToggleRequest): ApiResponse<CommunityToggleResponse> =
            activeService().toggleFavorite(contentId, request)

        override suspend fun listComments(contentId: Long, cursor: Long, limit: Int): ApiResponse<CommunityCommentListResponse> =
            activeService().listComments(contentId, cursor, limit)

        override suspend fun createComment(contentId: Long, request: CommunityCreateCommentRequest): ApiResponse<CommunityCreateCommentResponse> =
            activeService().createComment(contentId, request)

        override suspend fun deleteComment(commentId: Long): ApiResponse<Boolean> =
            activeService().deleteComment(commentId)

        override suspend fun myFavorites(cursor: Long, limit: Int): ApiResponse<CommunityFeedResponse> =
            activeService().myFavorites(cursor, limit)

        override suspend fun myPublished(cursor: Long, limit: Int): ApiResponse<CommunityFeedResponse> =
            activeService().myPublished(cursor, limit)

        override suspend fun hideContent(contentId: Long): ApiResponse<Boolean> =
            activeService().hideContent(contentId)

        override suspend fun deleteContent(contentId: Long): ApiResponse<Boolean> =
            activeService().deleteContent(contentId)
    }

    val authRepository = AuthRepository(apiService, preferences)
    val worksRepository = WorksRepository(apiService, authRepository, preferences)
    val taskRepository = TaskRepository(apiService, authRepository, preferences)
    val communityRepository = CommunityRepository(apiService, authRepository)

    init {
        scope.launch {
            preferences.tokenFlow.collectLatest { currentToken = it }
        }
        scope.launch {
            preferences.baseUrlFlow.collectLatest { currentBaseUrl = it }
        }
    }
}

