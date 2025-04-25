package com.haircloud.data.storage

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth_preferences")

object TokenKeys {
    val AUTH_TOKEN = stringPreferencesKey("auth_token")
    val USER_ID = intPreferencesKey("user_id")
}

class TokenManager(private val context: Context) {

    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TokenKeys.AUTH_TOKEN]
    }

    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[TokenKeys.USER_ID]
    }

    suspend fun saveSession(token: String, userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[TokenKeys.AUTH_TOKEN] = token
            preferences[TokenKeys.USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
