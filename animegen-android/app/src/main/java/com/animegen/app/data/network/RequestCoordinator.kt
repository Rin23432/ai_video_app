package com.animegen.app.data.network

import com.animegen.app.data.repo.AppResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RequestCoordinator {
    private val mutex = Mutex()
    private val inflight = mutableMapOf<String, Deferred<AppResult<*>>>()

    suspend fun <T> coalesce(key: String, block: suspend () -> AppResult<T>): AppResult<T> {
        val existing = mutex.withLock { inflight[key] }
        if (existing != null) {
            @Suppress("UNCHECKED_CAST")
            return existing.await() as AppResult<T>
        }

        val deferred = CompletableDeferred<AppResult<T>>()
        val winner = mutex.withLock {
            val active = inflight[key]
            if (active == null) {
                inflight[key] = deferred
                deferred
            } else {
                active
            }
        }

        if (winner !== deferred) {
            @Suppress("UNCHECKED_CAST")
            return winner.await() as AppResult<T>
        }

        try {
            val result = block()
            deferred.complete(result)
            return result
        } catch (ex: Exception) {
            deferred.completeExceptionally(ex)
            throw ex
        } finally {
            mutex.withLock {
                if (inflight[key] === deferred) {
                    inflight.remove(key)
                }
            }
        }
    }
}
