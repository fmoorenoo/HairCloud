package com.haircloud.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

// Capa intermedia entre el viewModel y la API
class UserRepository {
    private val api = ApiClient.instance

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.login(LoginRequest(username, password)).execute()
            }
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error en el login"))
            }
        } catch (e: HttpException) {
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
                Result.failure(Exception("Error en el registro"))
            }
        } catch (e: HttpException) {
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
                val errorJson = e.response()?.errorBody()?.string()
                JSONObject(errorJson).getString("error")
            } catch (ex: Exception) {
                "Error desconocido"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
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
