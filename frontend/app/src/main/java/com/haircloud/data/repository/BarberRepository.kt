package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.ApiResponse
import com.haircloud.data.model.GetBarberResponse
import com.haircloud.data.model.BarberDate
import com.haircloud.data.model.CreateBarberRequest
import com.haircloud.data.model.InactiveBarberResponse
import com.haircloud.data.model.WorkDaySchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class BarberRepository {
    private val api = ApiClient.instance

    suspend fun getBarber(usuarioId: Int): Result<GetBarberResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getBarber(usuarioId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = try {
                    val errorJson = response.errorBody()?.string() ?: "{}"
                    JSONObject(errorJson).getString("error")
                } catch (_: Exception) {
                    "Error al obtener el barbero"
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

    suspend fun getBarberDates(barberId: Int, date: String): Result<List<BarberDate>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getBarberDates(barberId, date).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()?.dates ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener las citas del barbero"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBarberDatesInRange(barberId: Int, startDate: String, endDate: String): Result<List<BarberDate>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getBarberDatesInRange(barberId, startDate, endDate).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body()?.dates ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener las citas por rango"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBarber(usuarioId: Int, updateData: Map<String, String?>): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.updateBarber(usuarioId, updateData).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = try {
                    val errorJson = response.errorBody()?.string() ?: "{}"
                    JSONObject(errorJson).getString("error")
                } catch (_: Exception) {
                    "Error al actualizar el barbero"
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

    suspend fun toggleBarberRole(usuarioId: Int): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.toggleBarberRole(usuarioId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorJson = response.errorBody()?.string() ?: "{}"
                val message = JSONObject(errorJson).optString("error", "Error al cambiar el rol")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deactivateBarber(usuarioId: Int): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.deactivateBarber(usuarioId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorJson = response.errorBody()?.string() ?: "{}"
                val message = JSONObject(errorJson).optString("error", "Error al desactivar el barbero")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun activateBarber(usuarioId: Int): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.activateBarber(usuarioId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorJson = response.errorBody()?.string() ?: "{}"
                val message = JSONObject(errorJson).optString("error", "Error al activar el barbero")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getInactiveBarbers(): Result<List<InactiveBarberResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getInactiveBarbers().execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                val errorJson = response.errorBody()?.string() ?: "{}"
                val message = JSONObject(errorJson).optString("error", "Error al obtener barberos inactivos")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createBarber(request: CreateBarberRequest): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.createBarber(request).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorJson = response.errorBody()?.string() ?: "{}"
                val message = JSONObject(errorJson).optString("error", "Error al crear barbero")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBarberSchedule(peluqueroId: Int, schedule: List<WorkDaySchedule>): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.updateBarberSchedule(peluqueroId, schedule).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorJson = response.errorBody()?.string() ?: "{}"
                val message = JSONObject(errorJson).optString("error", "Error al actualizar horario")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
