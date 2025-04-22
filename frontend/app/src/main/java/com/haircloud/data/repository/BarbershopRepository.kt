package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class BarbershopRepository {
    private val api = ApiClient.instance

    suspend fun getAllBarbershops(clienteId: Int): Result<List<BarbershopResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getAllBarbershops(clienteId).execute()
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

    suspend fun getBarbershopById(clienteId: Int, localId: Int): Result<BarbershopResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getBarbershopById(clienteId, localId).execute()
            }

            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("No se encontró la barbería"))
            } else {
                val errorMessage = try {
                    val errorJson = response.errorBody()?.string() ?: "{}"
                    JSONObject(errorJson).getString("error")
                } catch (_: Exception) {
                    "Error al obtener la barbería"
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

    suspend fun getFavoriteBarbershops(clienteId: Int): Result<List<BarbershopResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getFavoriteBarbershops(clienteId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorMessage = try {
                    val errorJson = response.errorBody()?.string() ?: "{}"
                    JSONObject(errorJson).getString("error")
                } catch (_: Exception) {
                    "Error al obtener favoritos"
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

    suspend fun getServicesById(localId: Int): Result<List<ServiceResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.instance.getServicesByLocalId(localId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener los servicios"))
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
                Result.failure(Exception("Error al obtener el servicio"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun getBarbershopReviews(localId: Int): Result<List<ReviewResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getBarbershopReviews(localId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener las reseñas"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun addReview(clienteId: Int, localId: Int, calificacion: Double, comentario: String): Result<String> {
        return try {
            val body = ReviewRequest(
                clienteid = clienteId,
                localid = localId,
                calificacion = calificacion,
                comentario = comentario
            )

            val response = withContext(Dispatchers.IO) {
                api.addReview(body).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Reseña añadida correctamente")
            } else {
                val errorMessage = try {
                    val errorJson = response.errorBody()?.string() ?: "{}"
                    JSONObject(errorJson).getString("error")
                } catch (_: Exception) {
                    "No se pudo añadir la reseña"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun deleteReview(resenaId: Int): Result<String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.deleteReview(resenaId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()?.message ?: "Reseña eliminada")
            } else {
                val errorMessage = try {
                    val errorJson = response.errorBody()?.string() ?: "{}"
                    JSONObject(errorJson).getString("error")
                } catch (_: Exception) {
                    "No se pudo eliminar la reseña"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }

    suspend fun getBarbersById(localId: Int): Result<List<BarberResponse>> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.getBarbersByLocalId(localId).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Error al obtener peluqueros"))
            }
        } catch (_: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }
}
