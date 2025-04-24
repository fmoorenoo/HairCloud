package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.ApiResponse
import com.haircloud.data.model.ClientResponse
import com.haircloud.data.model.ClientStatsResponse
import com.haircloud.data.model.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class ClientRepository {
    private val api = ApiClient.instance

    suspend fun getClient(usuarioId: Int): Result<ClientResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getClient(usuarioId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = try {
                    val errorJson = response.errorBody()?.string() ?: "{}"
                    JSONObject(errorJson).getString("error")
                } catch (_: Exception) {
                    "Error al obtener el cliente"
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

    suspend fun updateClient(usuarioId: Int, updateData: Map<String, String?>): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.updateClient(usuarioId, updateData).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = try {
                    val errorJson = response.errorBody()?.string() ?: "{}"
                    JSONObject(errorJson).getString("error")
                } catch (_: Exception) {
                    "Error al actualizar el cliente"
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

    suspend fun getClientDates(clienteId: Int): Result<List<Date>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.instance.getClientDates(clienteId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()?.appointments ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener las citas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClientStats(clienteId: Int): Result<ClientStatsResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.instance.getClientStats(clienteId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener estadísticas del cliente"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
