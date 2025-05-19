package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.ApiResponse
import com.haircloud.data.model.AddDateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Response

class DatesRepository {
    private val api = ApiClient.instance

    suspend fun addDate(request: AddDateRequest): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.addDate(request).execute()
            }

            handleApiResponse(response, "Error al añadir la cita")
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun deleteDate(citaId: Int): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.deleteDate(citaId).execute()
            }

            handleApiResponse(response, "Error al eliminar la cita")
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun updateDateEstado(citaId: Int, estado: String, motivo: String? = null): Result<ApiResponse> {
        val body = mutableMapOf<String, String>("estado" to estado)
        motivo?.let {
            body["motivo"] = it.trim()
        }
        return try {
            val response = withContext(Dispatchers.IO) {
                api.updateDateEstado(citaId, body).execute()
            }
            handleApiResponse(response, "Error al actualizar el estado de la cita")
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    private fun handleApiResponse(response: Response<ApiResponse>, errorMessage: String): Result<ApiResponse> {
        return if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            val errorBody = response.errorBody()?.string()
            val detailedMessage = try {
                JSONObject(errorBody ?: "").optString("error", errorMessage)
            } catch (_: Exception) {
                errorMessage
            }
            Result.failure(Exception(detailedMessage))
        }
    }
}
