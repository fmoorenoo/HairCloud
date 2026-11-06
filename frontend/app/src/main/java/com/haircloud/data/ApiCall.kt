package com.haircloud.data

import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

/** Error de la API con el mensaje legible y el código máquina que devuelve el servidor. */
class ApiException(
    message: String,
    val status: Int,
    val code: String? = null
) : Exception(message)

const val CONNECTION_ERROR = "No se pudo conectar con el servidor. Revisa tu conexión."

/**
 * Ejecuta una llamada a la API y la convierte en [Result].
 *
 * Sustituye a los ~40 bloques try/catch copiados en cada repositorio, que
 * además usaban `response.body()!!` (crash si el cuerpo venía vacío) y
 * llamadas bloqueantes `execute()`.
 */
suspend fun <T> apiCall(defaultError: String, block: suspend () -> Response<T>): Result<T> =
    withContext(Dispatchers.IO) {
        try {
            val response = block()
            val body = response.body()
            when {
                response.isSuccessful && body != null -> Result.success(body)
                response.isSuccessful -> Result.failure(ApiException(defaultError, response.code()))
                else -> Result.failure(parseError(response, defaultError))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: IOException) {
            Result.failure(ApiException(CONNECTION_ERROR, 0, "network"))
        } catch (_: Exception) {
            Result.failure(ApiException(defaultError, -1))
        }
    }

internal fun parseError(response: Response<*>, defaultError: String): ApiException {
    val raw = try {
        response.errorBody()?.string()
    } catch (_: Exception) {
        null
    }
    return parseErrorBody(raw, response.code(), defaultError)
}

internal fun parseErrorBody(raw: String?, status: Int, defaultError: String): ApiException {
    if (raw.isNullOrBlank()) {
        return when (status) {
            401 -> ApiException("Tu sesión ha caducado. Inicia sesión de nuevo.", status, "unauthorized")
            429 -> ApiException("Demasiadas peticiones. Inténtalo más tarde.", status, "rate_limited")
            else -> ApiException(if (status >= 500) "Error del servidor. Inténtalo más tarde." else defaultError, status)
        }
    }
    return fromJson(raw, status, defaultError)
}

private fun fromJson(raw: String?, status: Int, defaultError: String): ApiException = try {
    val json = JsonParser.parseString(raw ?: "").asJsonObject
    val message = json.get("error")?.takeIf { it.isJsonPrimitive }?.asString ?: defaultError
    val code = json.get("code")?.takeIf { it.isJsonPrimitive }?.asString
    ApiException(message, status, code)
} catch (_: Exception) {
    ApiException(if (status >= 500) "Error del servidor. Inténtalo más tarde." else defaultError, status)
}
