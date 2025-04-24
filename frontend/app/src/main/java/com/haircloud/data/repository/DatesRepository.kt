package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.ApiResponse
import com.haircloud.data.model.AddDateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    private fun handleApiResponse(response: Response<ApiResponse>, errorMessage: String): Result<ApiResponse> {
        return if (response.isSuccessful) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(errorMessage))
        }
    }
}
