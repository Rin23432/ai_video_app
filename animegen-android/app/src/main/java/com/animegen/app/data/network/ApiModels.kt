package com.animegen.app.data.network

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

data class GuestTokenRequest(val deviceId: String)
data class GuestTokenResponse(val token: String, val userId: Long)

data class CreateWorkRequest(
    val requestId: String,
    val title: String,
    val prompt: String,
    val styleId: String,
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

