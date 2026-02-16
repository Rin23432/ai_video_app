package com.animegen.app.data.network

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

data class AuthUserInfo(
    val userId: Long,
    val nickname: String,
    val role: String,
    val avatarUrl: String?
)

data class GuestTokenRequest(val deviceId: String)
data class GuestTokenResponse(val token: String, val user: AuthUserInfo)
data class LoginRequest(val username: String, val password: String)
data class RegisterRequest(val username: String, val password: String, val nickname: String)
data class AuthTokenResponse(val token: String, val user: AuthUserInfo)

data class UserStats(
    val published: Int,
    val favorites: Int,
    val likesReceived: Int
)

data class MeProfile(
    val userId: Long,
    val username: String?,
    val nickname: String,
    val avatarUrl: String?,
    val bio: String?,
    val role: String,
    val stats: UserStats
)

data class UpdateProfileRequest(
    val nickname: String,
    val bio: String?,
    val avatarUrl: String?
)

data class CreateWorkRequest(
    val requestId: String,
    val title: String,
    val prompt: String,
    val styleId: String,
    val apiKey: String,
    val aspectRatio: String,
    val durationSec: Int,
    val mode: String
)

data class CreateWorkResponse(val workId: Long, val taskId: Long)

data class TaskStatusResponse(
    val taskId: Long,
    val status: String,
    val progress: Int?,
    val stage: String?,
    val errorCode: String?,
    val errorMessage: String?
)

data class Work(
    val id: Long,
    val userId: Long,
    val title: String,
    val prompt: String,
    val styleId: String,
    val aspectRatio: String,
    val durationSec: Int,
    val status: String,
    val coverUrl: String?,
    val videoUrl: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class CommunityPublishRequest(
    val workId: Long,
    val title: String,
    val description: String?,
    val tagIds: List<Long> = emptyList()
)

data class CommunityPublishResponse(
    val contentId: Long
)

data class CommunityAuthor(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?
)

data class CommunityContentSummary(
    val contentId: Long,
    val title: String,
    val coverUrl: String?,
    val author: CommunityAuthor,
    val likeCount: Int,
    val favoriteCount: Int,
    val commentCount: Int,
    val publishTime: String?
)

data class CommunityFeedResponse(
    val items: List<CommunityContentSummary>,
    val nextCursor: Long
)

data class CommunityTag(
    val tagId: Long,
    val name: String,
    val contentCount: Int,
    val hotScore: Long
)

data class CommunityTagListResponse(
    val items: List<CommunityTag>
)

data class CommunityTagDetail(
    val tagId: Long,
    val name: String,
    val description: String?,
    val contentCount: Int,
    val hotScore: Long
)

data class CommunityViewerState(
    val liked: Boolean,
    val favorited: Boolean
)

data class CommunityContentDetail(
    val contentId: Long,
    val workId: Long,
    val title: String,
    val description: String?,
    val mediaType: String,
    val coverUrl: String?,
    val mediaUrl: String?,
    val author: CommunityAuthor,
    val likeCount: Int,
    val favoriteCount: Int,
    val commentCount: Int,
    val viewerState: CommunityViewerState,
    val publishTime: String?
)

data class CommunityToggleRequest(val action: String = "TOGGLE")

data class CommunityToggleResponse(
    val liked: Boolean? = null,
    val favorited: Boolean? = null,
    val likeCount: Int? = null,
    val favoriteCount: Int? = null
)

data class CommunityComment(
    val commentId: Long,
    val user: CommunityAuthor,
    val text: String,
    val createdAt: String?
)

data class CommunityCommentListResponse(
    val items: List<CommunityComment>,
    val nextCursor: Long
)

data class CommunityCreateCommentRequest(
    val text: String
)

data class CommunityCreateCommentResponse(
    val commentId: Long,
    val commentCount: Int
)

