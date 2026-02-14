package com.animegen.app.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/api/v1/auth/guest")
    suspend fun guestToken(@Body request: GuestTokenRequest): ApiResponse<GuestTokenResponse>

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

