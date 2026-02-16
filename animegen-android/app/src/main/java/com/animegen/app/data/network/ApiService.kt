package com.animegen.app.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @POST("/api/v1/auth/guest")
    suspend fun guestToken(@Body request: GuestTokenRequest): ApiResponse<GuestTokenResponse>

    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthTokenResponse>

    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<AuthTokenResponse>

    @GET("/api/v1/me")
    suspend fun me(): ApiResponse<MeProfile>

    @PUT("/api/v1/me/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<MeProfile>

    @POST("/api/v1/works")
    suspend fun createWork(@Body request: CreateWorkRequest): ApiResponse<CreateWorkResponse>

    @GET("/api/v1/tasks/{taskId}")
    suspend fun getTask(@Path("taskId") taskId: Long): ApiResponse<TaskStatusResponse>

    @GET("/api/v1/works")
    suspend fun listWorks(
        @Query("cursor") cursor: Long = 0,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<Work>>

    @GET("/api/v1/works/{workId}")
    suspend fun getWork(@Path("workId") workId: Long): ApiResponse<Work>

    @POST("/api/v1/community/contents")
    suspend fun publishCommunity(@Body request: CommunityPublishRequest): ApiResponse<CommunityPublishResponse>

    @GET("/api/v1/community/contents")
    suspend fun listCommunityContents(
        @Query("tab") tab: String,
        @Query("cursor") cursor: Long = 0,
        @Query("limit") limit: Int = 20
    ): ApiResponse<CommunityFeedResponse>

    @GET("/api/v1/community/contents/{contentId}")
    suspend fun getCommunityDetail(@Path("contentId") contentId: Long): ApiResponse<CommunityContentDetail>

    @GET("/api/v1/community/tags/hot")
    suspend fun hotTags(
        @Query("limit") limit: Int = 20
    ): ApiResponse<CommunityTagListResponse>

    @GET("/api/v1/community/tags/search")
    suspend fun searchTags(
        @Query("keyword") keyword: String,
        @Query("limit") limit: Int = 20
    ): ApiResponse<CommunityTagListResponse>

    @GET("/api/v1/community/tags/{tagId}")
    suspend fun getTagDetail(@Path("tagId") tagId: Long): ApiResponse<CommunityTagDetail>

    @GET("/api/v1/community/tags/{tagId}/contents")
    suspend fun listTagContents(
        @Path("tagId") tagId: Long,
        @Query("tab") tab: String,
        @Query("cursor") cursor: Long = 0,
        @Query("limit") limit: Int = 20
    ): ApiResponse<CommunityFeedResponse>

    @POST("/api/v1/community/contents/{contentId}/like")
    suspend fun toggleLike(
        @Path("contentId") contentId: Long,
        @Body request: CommunityToggleRequest = CommunityToggleRequest()
    ): ApiResponse<CommunityToggleResponse>

    @POST("/api/v1/community/contents/{contentId}/favorite")
    suspend fun toggleFavorite(
        @Path("contentId") contentId: Long,
        @Body request: CommunityToggleRequest = CommunityToggleRequest()
    ): ApiResponse<CommunityToggleResponse>

    @GET("/api/v1/community/contents/{contentId}/comments")
    suspend fun listComments(
        @Path("contentId") contentId: Long,
        @Query("cursor") cursor: Long = 0,
        @Query("limit") limit: Int = 20
    ): ApiResponse<CommunityCommentListResponse>

    @POST("/api/v1/community/contents/{contentId}/comments")
    suspend fun createComment(
        @Path("contentId") contentId: Long,
        @Body request: CommunityCreateCommentRequest
    ): ApiResponse<CommunityCreateCommentResponse>

    @DELETE("/api/v1/community/comments/{commentId}")
    suspend fun deleteComment(@Path("commentId") commentId: Long): ApiResponse<Boolean>

    @GET("/api/v1/community/me/favorites")
    suspend fun myFavorites(
        @Query("cursor") cursor: Long = 0,
        @Query("limit") limit: Int = 20
    ): ApiResponse<CommunityFeedResponse>

    @GET("/api/v1/community/me/contents")
    suspend fun myPublished(
        @Query("cursor") cursor: Long = 0,
        @Query("limit") limit: Int = 20
    ): ApiResponse<CommunityFeedResponse>

    @POST("/api/v1/community/contents/{contentId}/hide")
    suspend fun hideContent(@Path("contentId") contentId: Long): ApiResponse<Boolean>

    @DELETE("/api/v1/community/contents/{contentId}")
    suspend fun deleteContent(@Path("contentId") contentId: Long): ApiResponse<Boolean>
}

