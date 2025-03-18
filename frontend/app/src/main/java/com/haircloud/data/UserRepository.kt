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
}
