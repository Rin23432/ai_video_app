package com.animegen.app.data.network

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

private data class MockTaskState(
    val taskId: Long,
    val workId: Long,
    var pollCount: Int = 0
)

class OfflineMockApiService : ApiService {
    private val nextUserId = AtomicLong(1000)
    private val nextWorkId = AtomicLong(10000)
    private val nextTaskId = AtomicLong(50000)
    private val nextContentId = AtomicLong(30000)
    private val nextCommentId = AtomicLong(90000)

    private val tasks = ConcurrentHashMap<Long, MockTaskState>()
    private val works = ConcurrentHashMap<Long, Work>()
    private val contents = ConcurrentHashMap<Long, CommunityContentDetail>()
    private val comments = ConcurrentHashMap<Long, MutableList<CommunityComment>>()
    private val likes = ConcurrentHashMap<Long, Boolean>()
    private val favorites = ConcurrentHashMap<Long, Boolean>()
    private var currentMe = MeProfile(
        userId = 1001,
        username = "offline_user",
        nickname = "Offline User",
        avatarUrl = null,
        bio = "offline profile",
        role = "GUEST",
        stats = UserStats(0, 0, 0)
    )
    private val tags = listOf(
        CommunityTag(1, "奇幻", 18, 80),
        CommunityTag(2, "校园", 12, 66),
        CommunityTag(3, "热血", 9, 55),
        CommunityTag(4, "搞笑", 7, 41),
        CommunityTag(5, "悬疑", 6, 37)
    )

    override suspend fun guestToken(request: GuestTokenRequest): ApiResponse<GuestTokenResponse> {
        val userId = nextUserId.incrementAndGet()
        val token = "offline-token-${request.deviceId.ifBlank { "device" }}"
        val user = AuthUserInfo(userId = userId, nickname = "Guest-$userId", role = "GUEST", avatarUrl = null)
        currentMe = currentMe.copy(userId = userId, username = null, nickname = user.nickname, role = "GUEST")
        return ApiResponse(code = 0, message = "ok", data = GuestTokenResponse(token = token, user = user))
    }

    override suspend fun login(request: LoginRequest): ApiResponse<AuthTokenResponse> {
        val user = AuthUserInfo(userId = 2001, nickname = request.username, role = "USER", avatarUrl = null)
        currentMe = currentMe.copy(
            userId = user.userId,
            username = request.username,
            nickname = request.username,
            role = "USER"
        )
        return ApiResponse(code = 0, message = "ok", data = AuthTokenResponse(token = "offline-login-token", user = user))
    }

    override suspend fun register(request: RegisterRequest): ApiResponse<AuthTokenResponse> {
        val userId = nextUserId.incrementAndGet()
        val user = AuthUserInfo(userId = userId, nickname = request.nickname, role = "USER", avatarUrl = null)
        currentMe = currentMe.copy(
            userId = userId,
            username = request.username,
            nickname = request.nickname,
            role = "USER"
        )
        return ApiResponse(code = 0, message = "ok", data = AuthTokenResponse(token = "offline-register-token-$userId", user = user))
    }

    override suspend fun me(): ApiResponse<MeProfile> {
        val published = contents.values.count { it.author.userId == currentMe.userId }
        val favoritesCount = favorites.values.count { it }
        val likesReceived = contents.values.filter { it.author.userId == currentMe.userId }.sumOf { it.likeCount }
        return ApiResponse(
            code = 0,
            message = "ok",
            data = currentMe.copy(stats = UserStats(published, favoritesCount, likesReceived))
        )
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): ApiResponse<MeProfile> {
        currentMe = currentMe.copy(nickname = request.nickname, bio = request.bio, avatarUrl = request.avatarUrl)
        return ApiResponse(code = 0, message = "ok", data = currentMe)
    }

    override suspend fun createWork(request: CreateWorkRequest): ApiResponse<CreateWorkResponse> {
        val now = Instant.now().toString()
        val workId = nextWorkId.incrementAndGet()
        val taskId = nextTaskId.incrementAndGet()
        works[workId] = Work(
            id = workId,
            userId = 1001,
            title = request.title,
            prompt = request.prompt,
            styleId = request.styleId,
            aspectRatio = request.aspectRatio,
            durationSec = request.durationSec,
            status = "PROCESSING",
            coverUrl = null,
            videoUrl = null,
            createdAt = now,
            updatedAt = now
        )
        tasks[taskId] = MockTaskState(taskId = taskId, workId = workId)
        return ApiResponse(code = 0, message = "ok", data = CreateWorkResponse(workId = workId, taskId = taskId))
    }

    override suspend fun getTask(taskId: Long): ApiResponse<TaskStatusResponse> {
        val task = tasks[taskId]
            ?: return ApiResponse(code = 404, message = "task not found", data = null)

        task.pollCount += 1
        val progress = when {
            task.pollCount <= 1 -> 10
            task.pollCount == 2 -> 45
            task.pollCount == 3 -> 80
            else -> 100
        }

        val status = if (progress >= 100) "SUCCESS" else "RUNNING"
        val stage = if (status == "SUCCESS") "completed" else "offline_mock_generating"

        if (status == "SUCCESS") {
            val oldWork = works[task.workId]
            if (oldWork != null) {
                works[task.workId] = oldWork.copy(
                    status = "READY",
                    updatedAt = Instant.now().toString()
                )
            }
        }

        return ApiResponse(
            code = 0,
            message = "ok",
            data = TaskStatusResponse(
                taskId = taskId,
                status = status,
                progress = progress,
                stage = stage,
                errorCode = null,
                errorMessage = null
            )
        )
    }

    override suspend fun listWorks(cursor: Long, limit: Int): ApiResponse<List<Work>> {
        val sorted = works.values
            .sortedByDescending { it.id }
            .drop(cursor.toInt().coerceAtLeast(0))
            .take(limit.coerceAtLeast(1))
        return ApiResponse(code = 0, message = "ok", data = sorted)
    }

    override suspend fun getWork(workId: Long): ApiResponse<Work> {
        val work = works[workId]
            ?: return ApiResponse(code = 404, message = "work not found", data = null)
        return ApiResponse(code = 0, message = "ok", data = work)
    }

    override suspend fun publishCommunity(request: CommunityPublishRequest): ApiResponse<CommunityPublishResponse> {
        if (isGuest()) return ApiResponse(40100, "login required", null)
        val work = works[request.workId]
            ?: return ApiResponse(code = 404, message = "work not found", data = null)
        val id = nextContentId.incrementAndGet()
        val author = CommunityAuthor(1001, "offline-user", null)
        contents[id] = CommunityContentDetail(
            contentId = id,
            workId = work.id,
            title = request.title,
            description = request.description,
            mediaType = "VIDEO",
            coverUrl = work.coverUrl,
            mediaUrl = work.videoUrl,
            author = author,
            likeCount = 0,
            favoriteCount = 0,
            commentCount = 0,
            viewerState = CommunityViewerState(false, false),
            publishTime = Instant.now().toString()
        )
        comments[id] = mutableListOf()
        return ApiResponse(0, "ok", CommunityPublishResponse(id))
    }

    override suspend fun listCommunityContents(tab: String, cursor: Long, limit: Int): ApiResponse<CommunityFeedResponse> {
        val rows = contents.values.sortedByDescending {
            if (tab.equals("hot", ignoreCase = true)) {
                it.likeCount * 2 + it.favoriteCount * 3 + it.commentCount * 5
            } else {
                it.contentId.toInt()
            }
        }
        val sliced = rows.drop(cursor.toInt().coerceAtLeast(0)).take(limit.coerceAtLeast(1))
        val items = sliced.map {
            CommunityContentSummary(
                contentId = it.contentId,
                title = it.title,
                coverUrl = it.coverUrl,
                author = it.author,
                likeCount = it.likeCount,
                favoriteCount = it.favoriteCount,
                commentCount = it.commentCount,
                publishTime = it.publishTime
            )
        }
        return ApiResponse(0, "ok", CommunityFeedResponse(items, cursor + items.size))
    }

    override suspend fun getCommunityDetail(contentId: Long): ApiResponse<CommunityContentDetail> {
        val data = contents[contentId] ?: return ApiResponse(404, "content not found", null)
        return ApiResponse(0, "ok", data)
    }

    override suspend fun hotTags(limit: Int): ApiResponse<CommunityTagListResponse> {
        return ApiResponse(0, "ok", CommunityTagListResponse(tags.take(limit.coerceAtLeast(1))))
    }

    override suspend fun searchTags(keyword: String, limit: Int): ApiResponse<CommunityTagListResponse> {
        val q = keyword.trim()
        val rows = if (q.isBlank()) emptyList() else tags.filter { it.name.contains(q, ignoreCase = true) }
        return ApiResponse(0, "ok", CommunityTagListResponse(rows.take(limit.coerceAtLeast(1))))
    }

    override suspend fun getTagDetail(tagId: Long): ApiResponse<CommunityTagDetail> {
        val tag = tags.firstOrNull { it.tagId == tagId } ?: return ApiResponse(404, "tag not found", null)
        return ApiResponse(0, "ok", CommunityTagDetail(tag.tagId, tag.name, "offline tag", tag.contentCount, tag.hotScore))
    }

    override suspend fun listTagContents(tagId: Long, tab: String, cursor: Long, limit: Int): ApiResponse<CommunityFeedResponse> {
        return listCommunityContents(tab, cursor, limit)
    }

    override suspend fun toggleLike(contentId: Long, request: CommunityToggleRequest): ApiResponse<CommunityToggleResponse> {
        if (isGuest()) return ApiResponse(40100, "login required", null)
        val old = contents[contentId] ?: return ApiResponse(404, "content not found", null)
        val liked = !(likes[contentId] ?: false)
        likes[contentId] = liked
        val newCount = (old.likeCount + if (liked) 1 else -1).coerceAtLeast(0)
        contents[contentId] = old.copy(
            likeCount = newCount,
            viewerState = old.viewerState.copy(liked = liked)
        )
        return ApiResponse(0, "ok", CommunityToggleResponse(liked = liked, likeCount = newCount))
    }

    override suspend fun toggleFavorite(contentId: Long, request: CommunityToggleRequest): ApiResponse<CommunityToggleResponse> {
        if (isGuest()) return ApiResponse(40100, "login required", null)
        val old = contents[contentId] ?: return ApiResponse(404, "content not found", null)
        val favorited = !(favorites[contentId] ?: false)
        favorites[contentId] = favorited
        val newCount = (old.favoriteCount + if (favorited) 1 else -1).coerceAtLeast(0)
        contents[contentId] = old.copy(
            favoriteCount = newCount,
            viewerState = old.viewerState.copy(favorited = favorited)
        )
        return ApiResponse(0, "ok", CommunityToggleResponse(favorited = favorited, favoriteCount = newCount))
    }

    override suspend fun listComments(contentId: Long, cursor: Long, limit: Int): ApiResponse<CommunityCommentListResponse> {
        val list = comments[contentId] ?: mutableListOf()
        val items = list.drop(cursor.toInt().coerceAtLeast(0)).take(limit.coerceAtLeast(1))
        return ApiResponse(0, "ok", CommunityCommentListResponse(items, cursor + items.size))
    }

    override suspend fun createComment(contentId: Long, request: CommunityCreateCommentRequest): ApiResponse<CommunityCreateCommentResponse> {
        if (isGuest()) return ApiResponse(40100, "login required", null)
        val old = contents[contentId] ?: return ApiResponse(404, "content not found", null)
        val list = comments.getOrPut(contentId) { mutableListOf() }
        val comment = CommunityComment(
            commentId = nextCommentId.incrementAndGet(),
            user = CommunityAuthor(1001, "offline-user", null),
            text = request.text,
            createdAt = Instant.now().toString()
        )
        list.add(0, comment)
        contents[contentId] = old.copy(commentCount = old.commentCount + 1)
        return ApiResponse(0, "ok", CommunityCreateCommentResponse(comment.commentId, old.commentCount + 1))
    }

    override suspend fun deleteComment(commentId: Long): ApiResponse<Boolean> {
        comments.forEach { (_, list) -> list.removeIf { it.commentId == commentId } }
        return ApiResponse(0, "ok", true)
    }

    override suspend fun myFavorites(cursor: Long, limit: Int): ApiResponse<CommunityFeedResponse> {
        if (isGuest()) return ApiResponse(40100, "login required", null)
        return listCommunityContents("hot", cursor, limit)
    }

    override suspend fun myPublished(cursor: Long, limit: Int): ApiResponse<CommunityFeedResponse> {
        if (isGuest()) return ApiResponse(40100, "login required", null)
        return listCommunityContents("latest", cursor, limit)
    }

    override suspend fun hideContent(contentId: Long): ApiResponse<Boolean> {
        if (isGuest()) return ApiResponse(40100, "login required", null)
        contents.remove(contentId)
        return ApiResponse(0, "ok", true)
    }

    override suspend fun deleteContent(contentId: Long): ApiResponse<Boolean> {
        if (isGuest()) return ApiResponse(40100, "login required", null)
        contents.remove(contentId)
        comments.remove(contentId)
        likes.remove(contentId)
        favorites.remove(contentId)
        return ApiResponse(0, "ok", true)
    }

    private fun isGuest(): Boolean {
        return currentMe.role.equals("GUEST", ignoreCase = true)
    }
}
