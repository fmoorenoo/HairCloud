package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.ApiResponse
import com.haircloud.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

// Capa intermedia entre el viewModel y la API
class AuthRepository {
    private val api = ApiClient.instance

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.login(LoginRequest(username, password)).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "{}"
                val errorMsg = try {
                    JSONObject(errorBody).optString("error", "Error desconocido")
                } catch (_: Exception) {
                    "Error desconocido"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: HttpException) {
            val errorMsg = try {
                val errorBody = e.response()?.errorBody()?.string() ?: "{}"
                JSONObject(errorBody).optString("error", "Error HTTP")
            } catch (_: Exception) {
                "Error HTTP"
            }
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(nombre: String, email: String, username: String, password: String): Result<RegisterResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.register(RegisterRequest(nombre, email, username, password)).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "{}"
                val errorMessage = try {
                    val jsonObject = JSONObject(errorBody)
                    jsonObject.getString("error")
                } catch (_: Exception) {
                    "Error en el registro"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: "{}"
            val errorMessage = try {
                val jsonObject = JSONObject(errorBody)
                jsonObject.getString("error")
            } catch (_: Exception) {
                "Error en el registro"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Solicitar un código de verificación (recuperar contraseña o verificar email)
    suspend fun sendVerificationCode(email: String, purpose: String): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.sendVerificationCode(mapOf("email" to email, "purpose" to purpose))
            }
            Result.success(response)
        } catch (e: HttpException) {
            val errorMessage = try {
                val errorJson = e.response()?.errorBody()?.string() ?: "{}"
                JSONObject(errorJson).getString("error")
            } catch (_: Exception) {
                "Error desconocido"
            }
            Result.failure(Exception(errorMessage))
        } catch (_: Exception) {
            Result.failure(Exception("Error en la conexión"))
        }
    }

    // Verificar el código recibido (recuperar contraseña o verificar email)
    suspend fun verifyCode(email: String, code: String, purpose: String): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.verifyCode(mapOf("email" to email, "codigo" to code, "purpose" to purpose))
            }
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(e)
        }
    }

    // Restablecer la contraseña
    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.resetPassword(mapOf("email" to email, "codigo" to code, "password" to newPassword))
            }
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(e)
        }
    }
}
