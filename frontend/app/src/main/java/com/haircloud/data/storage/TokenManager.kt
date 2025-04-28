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
    val USER_ROLE = stringPreferencesKey("user_role")
}

class TokenManager(private val context: Context) {

    val token: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TokenKeys.AUTH_TOKEN]
    }

    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[TokenKeys.USER_ID]
    }

    val role: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TokenKeys.USER_ROLE]
    }

    suspend fun saveSession(token: String, userId: Int, role: String) {
        context.dataStore.edit { preferences ->
            preferences[TokenKeys.AUTH_TOKEN] = token
            preferences[TokenKeys.USER_ID] = userId
            preferences[TokenKeys.USER_ROLE] = role
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
