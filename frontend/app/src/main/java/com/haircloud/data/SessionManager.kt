package com.haircloud.data

import android.content.Context
import com.haircloud.data.storage.Session
import com.haircloud.data.storage.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking

/**
 * Estado de la sesión en memoria, compartido entre la capa de red (hilos de
 * OkHttp) y la UI. Persiste a través de [TokenManager].
 */
object SessionManager {
    private lateinit var tokenManager: TokenManager

    @Volatile
    private var cached: Session? = null

    @Volatile
    private var loaded = false

    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Ámbito que sobrevive a las pantallas (p. ej. para revocar la sesión al cerrar sesión). */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Emite cuando la sesión deja de ser válida y hay que volver al login. */
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    fun init(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    val store: TokenManager get() = tokenManager

    /** Lectura síncrona para los interceptores de OkHttp (se ejecutan en hilos de fondo). */
    fun current(): Session? {
        if (!loaded) {
            synchronized(this) {
                if (!loaded) {
                    cached = runBlocking { tokenManager.load() }
                    loaded = true
                }
            }
        }
        return cached
    }

    suspend fun save(session: Session) {
        tokenManager.saveSession(session)
        cached = session
        loaded = true
    }

    fun saveBlocking(session: Session) = runBlocking { save(session) }

    suspend fun clear() {
        cached = null
        loaded = true
        tokenManager.clearSession()
    }

    /** Cierra la sesión local y avisa a la UI (token revocado o caducado). */
    fun expire() {
        runBlocking { clear() }
        _expired.tryEmit(Unit)
    }
}
