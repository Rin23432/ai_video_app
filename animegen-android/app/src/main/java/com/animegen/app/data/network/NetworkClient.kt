package com.animegen.app.data.network

import com.animegen.app.data.repo.AppError
import com.animegen.app.data.repo.AppResult
import com.animegen.app.observability.Telemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.math.max

class HostRewriteInterceptor(
    private val baseUrlProvider: () -> String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val targetBase = baseUrlProvider().toHttpUrlOrNull()
        if (targetBase == null) return chain.proceed(request)

        val oldUrl = request.url
        val newUrl = oldUrl.newBuilder()
            .scheme(targetBase.scheme)
            .host(targetBase.host)
            .port(targetBase.port)
            .build()
        return chain.proceed(request.newBuilder().url(newUrl).build())
    }
}

class AuthHeaderInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val token = tokenProvider()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}

class MetricsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val startNs = System.nanoTime()
        return try {
            val response = chain.proceed(request)
            val durationMs = max(0L, (System.nanoTime() - startNs) / 1_000_000L)
            Telemetry.event(
                "api_call",
                mapOf(
                    "method" to request.method,
                    "path" to request.url.encodedPath,
                    "status" to response.code.toString(),
                    "durationMs" to durationMs.toString()
                )
            )
            response
        } catch (ex: Exception) {
            val durationMs = max(0L, (System.nanoTime() - startNs) / 1_000_000L)
            Telemetry.event(
                "api_call",
                mapOf(
                    "method" to request.method,
                    "path" to request.url.encodedPath,
                    "status" to "EX",
                    "durationMs" to durationMs.toString(),
                    "error" to (ex.javaClass.simpleName ?: "Exception")
                )
            )
            throw ex
        }
    }
}

class NetworkClient(
    baseUrlProvider: () -> String,
    tokenProvider: () -> String?
) {
    val apiService: ApiService

    init {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val okHttp = OkHttpClient.Builder()
            .addInterceptor(HostRewriteInterceptor(baseUrlProvider))
            .addInterceptor(AuthHeaderInterceptor(tokenProvider))
            .addInterceptor(MetricsInterceptor())
            .addInterceptor(logging)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl("http://127.0.0.1/")
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

suspend fun <T> safeApiCall(call: suspend () -> ApiResponse<T>): AppResult<T> {
    return withContext(Dispatchers.IO) {
        try {
            val response = call()
            if (response.code == 0 && response.data != null) {
                AppResult.Success(response.data)
            } else if (response.code == 40100) {
                AppResult.Failure(AppError.LoginRequired(response.message.ifBlank { "请先登录后再操作" }))
            } else {
                AppResult.Failure(AppError.Business(response.code, response.message.ifBlank { "业务请求失败" }))
            }
        } catch (e: IOException) {
            AppResult.Failure(AppError.Network())
        } catch (e: HttpException) {
            when {
                e.code() == 401 -> AppResult.Failure(AppError.Unauthorized())
                e.code() >= 500 -> AppResult.Failure(AppError.Server())
                else -> AppResult.Failure(AppError.Business(e.code(), "请求失败(${e.code()})"))
            }
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e.message ?: "未知异常"))
        }
    }
}

