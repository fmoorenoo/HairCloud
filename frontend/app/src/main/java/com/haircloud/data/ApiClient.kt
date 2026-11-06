package com.haircloud.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.haircloud.BuildConfig
import com.haircloud.data.storage.Session
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP de la app.
 *
 * - La URL base sale de BuildConfig (configurable por tipo de build) en vez
 *   de una constante que había que editar a mano.
 * - [AuthInterceptor] añade el access token a cada petición (antes el token se
 *   guardaba pero nunca se enviaba).
 * - [TokenAuthenticator] renueva el access token caducado con el refresh token
 *   de forma transparente y sincronizada entre peticiones concurrentes.
 * - El log HTTP solo existe en debug y oculta cabeceras de autenticación (antes
 *   se registraban en Logcat los cuerpos completos, contraseñas incluidas).
 */
object ApiClient {
    const val NO_AUTH_HEADER = "No-Auth"
    private val gson = Gson()

    private val baseClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val client: OkHttpClient by lazy {
        val builder = baseClient.newBuilder()
            .addInterceptor(AuthInterceptor())
            .authenticator(TokenAuthenticator(baseClient, gson))
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
                redactHeader("Authorization")
            })
        }
        builder.build()
    }

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}

internal class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.header(ApiClient.NO_AUTH_HEADER) != null) {
            return chain.proceed(original.newBuilder().removeHeader(ApiClient.NO_AUTH_HEADER).build())
        }
        val session = SessionManager.current() ?: return chain.proceed(original)
        return chain.proceed(
            original.newBuilder().header("Authorization", "Bearer ${session.accessToken}").build()
        )
    }
}

internal class TokenAuthenticator(
    private val http: OkHttpClient,
    private val gson: Gson
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            ?: return null // petición sin sesión (login, etc.): no hay nada que renovar
        if (responseCount(response) >= 2) return null

        synchronized(lock) {
            val session = SessionManager.current() ?: return null
            // Otra petición concurrente ya renovó el token: reintentar con el nuevo.
            if (session.accessToken != failedToken) {
                return response.request.withToken(session.accessToken)
            }
            return when (val result = refresh(session)) {
                is RefreshResult.Ok -> response.request.withToken(result.session.accessToken)
                RefreshResult.Rejected -> {
                    SessionManager.expire()
                    null
                }
                RefreshResult.NetworkError -> null
            }
        }
    }

    private fun refresh(session: Session): RefreshResult {
        val body = gson.toJson(mapOf("refresh_token" to session.refreshToken))
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(BuildConfig.API_BASE_URL + "auth/refresh")
            .post(body)
            .build()
        return try {
            http.newCall(request).execute().use { resp ->
                // Solo un rechazo explícito cierra la sesión; un 429 o un 5xx es
                // transitorio y no debe expulsar al usuario.
                if (resp.code == 401 || resp.code == 403) return RefreshResult.Rejected
                if (!resp.isSuccessful) return RefreshResult.NetworkError
                val json = gson.fromJson(resp.body?.string(), JsonObject::class.java)
                val renewed = Session(
                    accessToken = json["access_token"].asString,
                    refreshToken = json["refresh_token"].asString,
                    userId = json["usuarioid"].asInt,
                    role = json["rol"].asString
                )
                SessionManager.saveBlocking(renewed)
                RefreshResult.Ok(renewed)
            }
        } catch (_: IOException) {
            RefreshResult.NetworkError
        } catch (_: Exception) {
            RefreshResult.Rejected
        }
    }

    private fun Request.withToken(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private sealed interface RefreshResult {
        data class Ok(val session: Session) : RefreshResult
        data object Rejected : RefreshResult
        data object NetworkError : RefreshResult
    }
}
