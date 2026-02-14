package com.animegen.app.data.repo

import com.animegen.app.data.network.*

class CommunityRepository(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) {
    suspend fun publish(workId: Long, title: String, description: String?): AppResult<CommunityPublishResponse> {
        return authorizedCall { safeApiCall { apiService.publishCommunity(CommunityPublishRequest(workId, title, description)) } }
    }

    suspend fun feed(tab: String, cursor: Long = 0, limit: Int = 20): AppResult<CommunityFeedResponse> {
        return authorizedCall { safeApiCall { apiService.listCommunityContents(tab, cursor, limit) } }
    }

    suspend fun detail(contentId: Long): AppResult<CommunityContentDetail> {
        return authorizedCall { safeApiCall { apiService.getCommunityDetail(contentId) } }
    }

    suspend fun toggleLike(contentId: Long): AppResult<CommunityToggleResponse> {
        return authorizedCall { safeApiCall { apiService.toggleLike(contentId) } }
    }

    suspend fun toggleFavorite(contentId: Long): AppResult<CommunityToggleResponse> {
        return authorizedCall { safeApiCall { apiService.toggleFavorite(contentId) } }
    }

    suspend fun comments(contentId: Long, cursor: Long = 0, limit: Int = 20): AppResult<CommunityCommentListResponse> {
        return authorizedCall { safeApiCall { apiService.listComments(contentId, cursor, limit) } }
    }

    suspend fun createComment(contentId: Long, text: String): AppResult<CommunityCreateCommentResponse> {
        return authorizedCall { safeApiCall { apiService.createComment(contentId, CommunityCreateCommentRequest(text)) } }
    }

    suspend fun myFavorites(cursor: Long = 0, limit: Int = 20): AppResult<CommunityFeedResponse> {
        return authorizedCall { safeApiCall { apiService.myFavorites(cursor, limit) } }
    }

    suspend fun myPublished(cursor: Long = 0, limit: Int = 20): AppResult<CommunityFeedResponse> {
        return authorizedCall { safeApiCall { apiService.myPublished(cursor, limit) } }
    }

    suspend fun hide(contentId: Long): AppResult<Boolean> {
        return authorizedCall { safeApiCall { apiService.hideContent(contentId) } }
    }

    suspend fun remove(contentId: Long): AppResult<Boolean> {
        return authorizedCall { safeApiCall { apiService.deleteContent(contentId) } }
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
