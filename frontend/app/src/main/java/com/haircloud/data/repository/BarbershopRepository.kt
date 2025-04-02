package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.model.BarbershopResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class BarbershopRepository {
    private val api = ApiClient.instance

    suspend fun getBarbershops(): Result<List<BarbershopResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getBarbershops().execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = try {
                    val errorJson = response.errorBody()?.string() ?: "{}"
                    JSONObject(errorJson).getString("error")
                } catch (_: Exception) {
                    "Error al obtener las barberías"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = try {
                val errorJson = e.response()?.errorBody()?.string() ?: "{}"
                JSONObject(errorJson).getString("error")
            } catch (_: Exception) {
                "Error HTTP"
            }
            Result.failure(Exception(errorMessage))
        } catch (_: Exception) {
            Result.failure(Exception("Error en la conexión"))
        }
    }
}
