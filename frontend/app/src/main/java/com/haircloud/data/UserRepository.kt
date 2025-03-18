package com.haircloud.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    // Solicitar un código de recuperación de contraseña
    suspend fun forgotPassword(email: String): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.forgotPassword(mapOf("email" to email))
            }
            Result.success(response)
        } catch (e: HttpException) {
            Result.failure(e)
        }
    }

    // Verificar el código recibido por email
    suspend fun verifyCode(email: String, code: String): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.verifyCode(mapOf("email" to email, "codigo" to code))
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
