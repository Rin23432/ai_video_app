package com.animegen.app.data.network

import retrofit2.http.Body
import retrofit2.http.GET
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
}

