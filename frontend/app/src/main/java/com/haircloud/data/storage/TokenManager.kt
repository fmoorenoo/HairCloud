package com.haircloud.data.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "auth_preferences")

object TokenKeys {
    // Clave antigua: el JWT se guardaba en claro. Se elimina al migrar.
    val LEGACY_AUTH_TOKEN = stringPreferencesKey("auth_token")
    val ACCESS_TOKEN = stringPreferencesKey("access_token_enc")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token_enc")
    val USER_ID = intPreferencesKey("user_id")
    val USER_ROLE = stringPreferencesKey("user_role")
}

data class Session(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int,
    val role: String
)

/** Persistencia de la sesión. Los tokens se guardan cifrados con [TokenCipher]. */
class TokenManager(context: Context, private val cipher: TokenCipher = TokenCipher()) {
    private val store = context.applicationContext.dataStore

    /** Refresh token descifrado: su presencia indica que hay sesión. */
    val token: Flow<String?> = store.data.map { prefs ->
        prefs[TokenKeys.REFRESH_TOKEN]?.let(cipher::decrypt)
    }

    val userId: Flow<Int?> = store.data.map { it[TokenKeys.USER_ID] }

    val role: Flow<String?> = store.data.map { it[TokenKeys.USER_ROLE] }

    suspend fun load(): Session? {
        val prefs = store.data.first()
        val access = prefs[TokenKeys.ACCESS_TOKEN]?.let(cipher::decrypt)
        val refresh = prefs[TokenKeys.REFRESH_TOKEN]?.let(cipher::decrypt)
        val userId = prefs[TokenKeys.USER_ID]
        val role = prefs[TokenKeys.USER_ROLE]
        if (prefs[TokenKeys.LEGACY_AUTH_TOKEN] != null) {
            // Sesión de la versión anterior (JWT en claro, sin refresh token): se descarta.
            clearSession()
            return null
        }
        if (access == null || refresh == null || userId == null || role == null) return null
        return Session(access, refresh, userId, role)
    }

    suspend fun saveSession(session: Session) {
        store.edit { prefs ->
            prefs[TokenKeys.ACCESS_TOKEN] = cipher.encrypt(session.accessToken)
            prefs[TokenKeys.REFRESH_TOKEN] = cipher.encrypt(session.refreshToken)
            prefs[TokenKeys.USER_ID] = session.userId
            prefs[TokenKeys.USER_ROLE] = session.role
        }
    }

    suspend fun clearSession() {
        store.edit { it.clear() }
    }
}
