package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class ServicesRepository {
    private val api = ApiClient.instance

    suspend fun getServicesByLocalId(localId: Int): Result<List<ServiceResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getServicesByLocalId(localId).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener servicios"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun getService(servicioId: Int): Result<ServiceResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getService(servicioId).execute()
            }
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Servicio no encontrado"))
            } else {
                Result.failure(Exception("Error al obtener servicio"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun createService(service: ServiceRequest): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.createService(service).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Servicio creado correctamente")
            } else {
                val errorMessage = response.errorBody()?.string()?.let { raw ->
                    try {
                        JSONObject(raw).getString("message")
                    } catch (e: Exception) {
                        "Error al crear servicio"
                    }
                } ?: "Error al crear servicio"

                Result.failure(Exception(errorMessage))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }


    suspend fun editService(servicioId: Int, service: ServiceRequest): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.editService(servicioId, service).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Servicio actualizado")
            } else {
                Result.failure(Exception("Error al actualizar servicio"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun deleteService(servicioId: Int): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.deleteService(servicioId).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Servicio eliminado")
            } else {
                Result.failure(Exception("Error al eliminar servicio"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }
}
