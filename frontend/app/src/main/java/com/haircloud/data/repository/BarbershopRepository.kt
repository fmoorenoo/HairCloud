package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.model.BarbershopResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class BarbershopRepository {
    private val api = ApiClient.instance

    suspend fun getBarbershops(clienteId: Int): Result<List<BarbershopResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getBarbershops(clienteId).execute()
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

    suspend fun addFavorite(clienteId: Int, localId: Int): Result<String> {
        return try {
            val body = mapOf("clienteid" to clienteId, "localid" to localId)
            val response = withContext(Dispatchers.IO) {
                api.addFavorite(body).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Favorito agregado")
            } else {
                Result.failure(Exception("Error al agregar favorito"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error al conectar con el servidor"))
        }
    }

    suspend fun removeFavorite(clienteId: Int, localId: Int): Result<String> {
        return try {
            val body = mapOf("clienteid" to clienteId, "localid" to localId)
            val response = withContext(Dispatchers.IO) {
                api.removeFavorite(body).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Favorito eliminado")
            } else {
                Result.failure(Exception("Error al eliminar favorito"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error al conectar con el servidor"))
        }
    }
}
