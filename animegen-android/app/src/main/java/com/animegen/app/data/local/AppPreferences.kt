package com.animegen.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "animegen_prefs")

class AppPreferences(private val context: Context, defaultBaseUrl: String) {
    private object Keys {
        val token = stringPreferencesKey("token")
        val baseUrl = stringPreferencesKey("base_url")
        val deviceId = stringPreferencesKey("device_id")
        val userId = longPreferencesKey("user_id")
        val nickname = stringPreferencesKey("nickname")
        val role = stringPreferencesKey("role")
        val lastTaskId = longPreferencesKey("last_task_id")
        val lastWorkId = longPreferencesKey("last_work_id")
        val feedLatestJson = stringPreferencesKey("feed_latest_json")
        val feedHotJson = stringPreferencesKey("feed_hot_json")
        val feedLatestUpdatedAt = longPreferencesKey("feed_latest_updated_at")
        val feedHotUpdatedAt = longPreferencesKey("feed_hot_updated_at")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[Keys.token] }
    val baseUrlFlow: Flow<String> = context.dataStore.data.map { it[Keys.baseUrl] ?: defaultBaseUrl }
    val deviceIdFlow: Flow<String?> = context.dataStore.data.map { it[Keys.deviceId] }
    val userIdFlow: Flow<Long?> = context.dataStore.data.map { it[Keys.userId] }
    val nicknameFlow: Flow<String?> = context.dataStore.data.map { it[Keys.nickname] }
    val roleFlow: Flow<String?> = context.dataStore.data.map { it[Keys.role] }
    val lastTaskIdFlow: Flow<Long?> = context.dataStore.data.map { it[Keys.lastTaskId] }
    val lastWorkIdFlow: Flow<Long?> = context.dataStore.data.map { it[Keys.lastWorkId] }

    suspend fun setToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token.isNullOrBlank()) prefs.remove(Keys.token) else prefs[Keys.token] = token
        }
    }

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { it[Keys.baseUrl] = url }
    }

    suspend fun setDeviceId(deviceId: String) {
        context.dataStore.edit { it[Keys.deviceId] = deviceId }
    }

    suspend fun setUserInfo(userId: Long?, nickname: String?, role: String?) {
        context.dataStore.edit { prefs ->
            if (userId == null) prefs.remove(Keys.userId) else prefs[Keys.userId] = userId
            if (nickname.isNullOrBlank()) prefs.remove(Keys.nickname) else prefs[Keys.nickname] = nickname
            if (role.isNullOrBlank()) prefs.remove(Keys.role) else prefs[Keys.role] = role
        }
    }

    suspend fun setLastTask(taskId: Long?, workId: Long?) {
        context.dataStore.edit { prefs ->
            if (taskId == null) prefs.remove(Keys.lastTaskId) else prefs[Keys.lastTaskId] = taskId
            if (workId == null) prefs.remove(Keys.lastWorkId) else prefs[Keys.lastWorkId] = workId
        }
    }

    suspend fun setCommunityFeedCache(tab: String, json: String, updatedAt: Long = System.currentTimeMillis()) {
        context.dataStore.edit { prefs ->
            when (tab.lowercase()) {
                "hot" -> {
                    prefs[Keys.feedHotJson] = json
                    prefs[Keys.feedHotUpdatedAt] = updatedAt
                }
                else -> {
                    prefs[Keys.feedLatestJson] = json
                    prefs[Keys.feedLatestUpdatedAt] = updatedAt
                }
            }
        }
    }

    suspend fun getCommunityFeedCache(tab: String): Pair<String?, Long?> {
        val data = context.dataStore.data
        return data.map { prefs ->
            when (tab.lowercase()) {
                "hot" -> prefs[Keys.feedHotJson] to prefs[Keys.feedHotUpdatedAt]
                else -> prefs[Keys.feedLatestJson] to prefs[Keys.feedLatestUpdatedAt]
            }
        }
            .map { it.first to it.second }
            .first()
    }
}

