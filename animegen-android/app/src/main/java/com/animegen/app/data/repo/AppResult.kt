package com.animegen.app.data.repo

sealed class AppError(open val displayMessage: String) {
    data class Network(override val displayMessage: String = "网络连接失败，请检查网络后重试") : AppError(displayMessage)
    data class Unauthorized(override val displayMessage: String = "登录态失效，请重试") : AppError(displayMessage)
    data class Server(override val displayMessage: String = "服务暂时不可用，请稍后重试") : AppError(displayMessage)
    data class Business(val code: Int, override val displayMessage: String) : AppError(displayMessage)
    data class TaskFailed(override val displayMessage: String) : AppError(displayMessage)
    data class Unknown(override val displayMessage: String = "发生未知错误，请重试") : AppError(displayMessage)
}

sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Failure(val error: AppError) : AppResult<Nothing>()
}

