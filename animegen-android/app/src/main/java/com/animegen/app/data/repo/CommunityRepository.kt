package com.animegen.app.data.repo

import com.animegen.app.data.local.AppPreferences
import com.animegen.app.data.network.*
import com.animegen.app.data.network.RequestCoordinator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CommunityRepository(
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val preferences: AppPreferences,
    private val onLoginRequired: () -> Unit
) {
    private val requestCoordinator = RequestCoordinator()
    private val gson = Gson()
    private val summaryListType = object : TypeToken<List<CommunityContentSummary>>() {}.type

    suspend fun publish(workId: Long, title: String, description: String?, tagIds: List<Long>): AppResult<CommunityPublishResponse> {
        return authorizedCall(requireUserLogin = true) { safeApiCall { apiService.publishCommunity(CommunityPublishRequest(workId, title, description, tagIds)) } }
    }

    suspend fun feed(tab: String, cursor: Long = 0, limit: Int = 20): AppResult<CommunityFeedResponse> {
        val key = "feed:$tab:$cursor:$limit"
        return requestCoordinator.coalesce(key) {
            authorizedCall { safeApiCall { apiService.listCommunityContents(tab, cursor, limit) } }
        }.also { result ->
            if (cursor == 0L && result is AppResult.Success && (tab == "latest" || tab == "hot")) {
                val json = gson.toJson(result.data.items)
                preferences.setCommunityFeedCache(tab, json)
            }
        }
    }

    suspend fun feedCached(tab: String): List<CommunityContentSummary> {
        if (tab != "latest" && tab != "hot") return emptyList()
        val (json, _) = preferences.getCommunityFeedCache(tab)
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<CommunityContentSummary>>(json, summaryListType) ?: emptyList()
        }.getOrElse { emptyList() }
    }

    suspend fun searchContents(keyword: String, cursor: Long = 0, limit: Int = 20): AppResult<CommunityFeedResponse> {
        val q = keyword.trim()
        if (q.isBlank()) {
            return AppResult.Success(CommunityFeedResponse(emptyList(), 0))
        }
        val key = "search:$q:$cursor:$limit"
        return requestCoordinator.coalesce(key) {
            authorizedCall { safeApiCall { apiService.searchCommunityContents(q, cursor, limit) } }
        }.also { result ->
            if (cursor == 0L && result is AppResult.Success) {
                val json = gson.toJson(result.data.items)
                preferences.setCommunitySearchCache(q, json)
            }
        }
    }

    suspend fun searchContentsCached(keyword: String): List<CommunityContentSummary> {
        val q = keyword.trim()
        if (q.isBlank()) return emptyList()
        val (json, _) = preferences.getCommunitySearchCache(q)
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<CommunityContentSummary>>(json, summaryListType) ?: emptyList()
        }.getOrElse { emptyList() }
    }

    suspend fun detail(contentId: Long): AppResult<CommunityContentDetail> {
        return requestCoordinator.coalesce("detail:$contentId") {
            authorizedCall { safeApiCall { apiService.getCommunityDetail(contentId) } }
        }
    }

    suspend fun hotTags(limit: Int = 20): AppResult<CommunityTagListResponse> {
        return authorizedCall { safeApiCall { apiService.hotTags(limit) } }
    }

    suspend fun searchTags(keyword: String, limit: Int = 20): AppResult<CommunityTagListResponse> {
        return authorizedCall { safeApiCall { apiService.searchTags(keyword, limit) } }
    }

    suspend fun tagDetail(tagId: Long): AppResult<CommunityTagDetail> {
        return authorizedCall { safeApiCall { apiService.getTagDetail(tagId) } }
    }

    suspend fun tagContents(tagId: Long, tab: String, cursor: Long = 0, limit: Int = 20): AppResult<CommunityFeedResponse> {
        return authorizedCall { safeApiCall { apiService.listTagContents(tagId, tab, cursor, limit) } }
    }

    suspend fun contentRankings(window: String, cursor: Long = 0, limit: Int = 20): AppResult<CommunityRankingContentResponse> {
        return authorizedCall { safeApiCall { apiService.contentRankings(window, cursor, limit) } }
    }

    suspend fun authorRankings(window: String, cursor: Long = 0, limit: Int = 20): AppResult<CommunityRankingAuthorResponse> {
        return authorizedCall { safeApiCall { apiService.authorRankings(window, cursor, limit) } }
    }

    suspend fun tagRankings(window: String, cursor: Long = 0, limit: Int = 20): AppResult<CommunityRankingTagResponse> {
        return authorizedCall { safeApiCall { apiService.tagRankings(window, cursor, limit) } }
    }

    suspend fun toggleLike(contentId: Long): AppResult<CommunityToggleResponse> {
        return authorizedCall(requireUserLogin = true) { safeApiCall { apiService.toggleLike(contentId) } }
    }

    suspend fun toggleFavorite(contentId: Long): AppResult<CommunityToggleResponse> {
        return authorizedCall(requireUserLogin = true) { safeApiCall { apiService.toggleFavorite(contentId) } }
    }

    suspend fun comments(contentId: Long, cursor: Long = 0, limit: Int = 20): AppResult<CommunityCommentListResponse> {
        val key = "comments:$contentId:$cursor:$limit"
        return requestCoordinator.coalesce(key) {
            authorizedCall { safeApiCall { apiService.listComments(contentId, cursor, limit) } }
        }
    }

    suspend fun createComment(contentId: Long, text: String): AppResult<CommunityCreateCommentResponse> {
        return authorizedCall(requireUserLogin = true) { safeApiCall { apiService.createComment(contentId, CommunityCreateCommentRequest(text)) } }
    }

    suspend fun myFavorites(cursor: Long = 0, limit: Int = 20): AppResult<CommunityFeedResponse> {
        return authorizedCall(requireUserLogin = true) { safeApiCall { apiService.myFavorites(cursor, limit) } }
    }

    suspend fun myPublished(cursor: Long = 0, limit: Int = 20): AppResult<CommunityFeedResponse> {
        return authorizedCall(requireUserLogin = true) { safeApiCall { apiService.myPublished(cursor, limit) } }
    }

    suspend fun hide(contentId: Long): AppResult<Boolean> {
        return authorizedCall(requireUserLogin = true) { safeApiCall { apiService.hideContent(contentId) } }
    }

    suspend fun remove(contentId: Long): AppResult<Boolean> {
        return authorizedCall(requireUserLogin = true) { safeApiCall { apiService.deleteContent(contentId) } }
    }

    private suspend fun <T> authorizedCall(
        requireUserLogin: Boolean = false,
        block: suspend () -> AppResult<T>
    ): AppResult<T> {
        val auth = authRepository.ensureGuestToken()
        if (auth is AppResult.Failure) return auth
        val firstTry = block()
        if (
            requireUserLogin &&
            firstTry is AppResult.Failure &&
            (firstTry.error is AppError.LoginRequired || firstTry.error is AppError.Unauthorized)
        ) {
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
