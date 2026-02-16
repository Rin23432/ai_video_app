package com.animegen.app

import android.content.Context
import com.animegen.app.ai.FallbackTagSuggestEngine
import com.animegen.app.ai.LlamaCppTagSuggestEngine
import com.animegen.app.ai.LocalTagSuggestEngine
import com.animegen.app.ai.TagSuggestEngine
import com.animegen.app.ai.TfliteTagSuggestEngine
import com.animegen.app.data.local.AppPreferences
import com.animegen.app.data.network.ApiResponse
import com.animegen.app.data.network.ApiService
import com.animegen.app.data.network.AuthTokenResponse
import com.animegen.app.data.network.CreateWorkRequest
import com.animegen.app.data.network.CreateWorkResponse
import com.animegen.app.data.network.LoginRequest
import com.animegen.app.data.network.MeProfile
import com.animegen.app.data.network.GuestTokenRequest
import com.animegen.app.data.network.GuestTokenResponse
import com.animegen.app.data.network.CommunityCommentListResponse
import com.animegen.app.data.network.CommunityContentDetail
import com.animegen.app.data.network.CommunityCreateCommentRequest
import com.animegen.app.data.network.CommunityCreateCommentResponse
import com.animegen.app.data.network.CommunityFeedResponse
import com.animegen.app.data.network.CommunityPublishRequest
import com.animegen.app.data.network.CommunityPublishResponse
import com.animegen.app.data.network.CommunityTagDetail
import com.animegen.app.data.network.CommunityTagListResponse
import com.animegen.app.data.network.CommunityToggleRequest
import com.animegen.app.data.network.CommunityToggleResponse
import com.animegen.app.data.network.NetworkClient
import com.animegen.app.data.network.OfflineMockApiService
import com.animegen.app.data.network.RegisterRequest
import com.animegen.app.data.network.TaskStatusResponse
import com.animegen.app.data.network.UpdateProfileRequest
import com.animegen.app.data.network.Work
import com.animegen.app.data.repo.AuthRepository
import com.animegen.app.data.repo.CommunityRepository
import com.animegen.app.data.repo.TaskRepository
import com.animegen.app.data.repo.WorksRepository
import com.animegen.app.session.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var currentToken: String? = null

    @Volatile
    private var currentBaseUrl: String = BuildConfig.API_BASE_URL

    private val _loginRequiredFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    private val _authVersionFlow = MutableStateFlow(0L)
    private val _sessionStateFlow = MutableStateFlow<SessionState>(SessionState.Guest)

    val loginRequiredFlow: SharedFlow<Unit> = _loginRequiredFlow.asSharedFlow()
    val authVersionFlow: StateFlow<Long> = _authVersionFlow.asStateFlow()
    val sessionStateFlow: StateFlow<SessionState> = _sessionStateFlow.asStateFlow()

    val preferences = AppPreferences(context, BuildConfig.API_BASE_URL)
    val tagSuggestEngine: TagSuggestEngine = FallbackTagSuggestEngine(
        primary = LlamaCppTagSuggestEngine(context = context),
        fallback = FallbackTagSuggestEngine(
            primary = TfliteTagSuggestEngine(context = context, modelAssetName = "tag_suggest.tflite"),
            fallback = LocalTagSuggestEngine()
        )
    )

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

        override suspend fun login(request: LoginRequest): ApiResponse<AuthTokenResponse> =
            activeService().login(request)

        override suspend fun register(request: RegisterRequest): ApiResponse<AuthTokenResponse> =
            activeService().register(request)

        override suspend fun me(): ApiResponse<MeProfile> =
            activeService().me()

        override suspend fun updateProfile(request: UpdateProfileRequest): ApiResponse<MeProfile> =
            activeService().updateProfile(request)

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

        override suspend fun hotTags(limit: Int): ApiResponse<CommunityTagListResponse> =
            activeService().hotTags(limit)

        override suspend fun searchTags(keyword: String, limit: Int): ApiResponse<CommunityTagListResponse> =
            activeService().searchTags(keyword, limit)

        override suspend fun getTagDetail(tagId: Long): ApiResponse<CommunityTagDetail> =
            activeService().getTagDetail(tagId)

        override suspend fun listTagContents(tagId: Long, tab: String, cursor: Long, limit: Int): ApiResponse<CommunityFeedResponse> =
            activeService().listTagContents(tagId, tab, cursor, limit)

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

    val authRepository = AuthRepository(
        apiService = apiService,
        preferences = preferences,
        onLoginRequired = {
            _sessionStateFlow.value = SessionState.Expired
            _loginRequiredFlow.tryEmit(Unit)
        },
        onAuthChanged = { _authVersionFlow.value = _authVersionFlow.value + 1 }
    )
    val worksRepository = WorksRepository(apiService, authRepository, preferences) {
        _sessionStateFlow.value = SessionState.Expired
        _loginRequiredFlow.tryEmit(Unit)
    }
    val taskRepository = TaskRepository(apiService, authRepository, preferences) {
        _sessionStateFlow.value = SessionState.Expired
        _loginRequiredFlow.tryEmit(Unit)
    }
    val communityRepository = CommunityRepository(apiService, authRepository, preferences) {
        _sessionStateFlow.value = SessionState.Expired
        _loginRequiredFlow.tryEmit(Unit)
    }

    init {
        scope.launch {
            preferences.tokenFlow.collectLatest {
                currentToken = it
                _authVersionFlow.value = _authVersionFlow.value + 1
            }
        }
        scope.launch {
            combine(
                preferences.tokenFlow,
                preferences.userIdFlow,
                preferences.nicknameFlow,
                preferences.roleFlow
            ) { token, userId, nickname, role ->
                Quad(token, userId, nickname, role)
            }.collectLatest { (token, userId, nickname, role) ->
                _sessionStateFlow.value = when {
                    token.isNullOrBlank() -> SessionState.Guest
                    role.equals("USER", ignoreCase = true) && userId != null -> SessionState.User(userId, nickname)
                    role.equals("GUEST", ignoreCase = true) -> SessionState.Guest
                    else -> SessionState.Guest
                }
            }
        }
        scope.launch {
            preferences.baseUrlFlow.collectLatest { currentBaseUrl = it }
        }
    }
}

private data class Quad(
    val token: String?,
    val userId: Long?,
    val nickname: String?,
    val role: String?
)
