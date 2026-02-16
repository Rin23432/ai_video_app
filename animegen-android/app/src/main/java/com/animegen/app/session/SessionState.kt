package com.animegen.app.session

sealed class SessionState {
    data object Guest : SessionState()
    data class User(val userId: Long, val nickname: String?) : SessionState()
    data object Expired : SessionState()
}
