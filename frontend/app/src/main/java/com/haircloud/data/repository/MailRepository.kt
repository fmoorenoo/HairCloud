package com.haircloud.data.repository

import com.haircloud.data.ApiClient
import com.haircloud.data.ApiResponse
import com.haircloud.data.model.AddDateRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class MailRepository {
    private val api = ApiClient.instance

    suspend fun sendInfoDate(request: AddDateRequest): Result<ApiResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                api.sendInfoDate(request).execute()
            }

            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "{}"
                val errorMessage = try {
                    JSONObject(errorBody).getString("error")
                } catch (_: Exception) {
                    "Error al enviar el correo de confirmación"

                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: HttpException) {
            val errorMessage = try {
                val errorBody = e.response()?.errorBody()?.string() ?: "{}"
                JSONObject(errorBody).getString("error")
            } catch (_: Exception) {
                "Error HTTP al enviar el correo"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
