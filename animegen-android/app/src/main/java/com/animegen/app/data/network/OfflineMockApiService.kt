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

    private val tasks = ConcurrentHashMap<Long, MockTaskState>()
    private val works = ConcurrentHashMap<Long, Work>()

    override suspend fun guestToken(request: GuestTokenRequest): ApiResponse<GuestTokenResponse> {
        val userId = nextUserId.incrementAndGet()
        val token = "offline-token-${request.deviceId.ifBlank { "device" }}"
        return ApiResponse(code = 0, message = "ok", data = GuestTokenResponse(token = token, userId = userId))
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
            coverUrl = "https://picsum.photos/seed/$workId/720/1280",
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
        val stage = if (status == "SUCCESS") "生成完成" else "离线模拟生成中"

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
}

