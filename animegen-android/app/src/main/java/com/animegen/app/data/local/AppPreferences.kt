package com.animegen.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "animegen_prefs")

class AppPreferences(private val context: Context, defaultBaseUrl: String) {
    private object Keys {
        val token = stringPreferencesKey("token")
        val baseUrl = stringPreferencesKey("base_url")
        val deviceId = stringPreferencesKey("device_id")
        val lastTaskId = longPreferencesKey("last_task_id")
        val lastWorkId = longPreferencesKey("last_work_id")
    }

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[Keys.token] }
    val baseUrlFlow: Flow<String> = context.dataStore.data.map { it[Keys.baseUrl] ?: defaultBaseUrl }
    val deviceIdFlow: Flow<String?> = context.dataStore.data.map { it[Keys.deviceId] }
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

    suspend fun setLastTask(taskId: Long?, workId: Long?) {
        context.dataStore.edit { prefs ->
            if (taskId == null) prefs.remove(Keys.lastTaskId) else prefs[Keys.lastTaskId] = taskId
            if (workId == null) prefs.remove(Keys.lastWorkId) else prefs[Keys.lastWorkId] = workId
        }
    }
}

