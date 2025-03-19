package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.haircloud.data.UserRepository

class ForgotPasswordViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState

    fun sendVerificationCode(email: String) {
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            try {
                val result = repository.forgotPassword(email)
                if (result.isSuccess) {
                    _forgotPasswordState.value = ForgotPasswordState.CodeSentSuccess("Código enviado con éxito")
                } else {
                    _forgotPasswordState.value = ForgotPasswordState.CodeSentError("Error al enviar el código")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.CodeSentError(e.message ?: "Error desconocido")
            }
        }
    }

    fun verifyCode(email: String, code: String) {
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            try {
                val result = repository.verifyCode(email, code)
                if (result.isSuccess) {
                    _forgotPasswordState.value = ForgotPasswordState.CodeVerifiedSuccess("Código verificado correctamente")
                } else {
                    _forgotPasswordState.value = ForgotPasswordState.CodeVerifiedError("Código incorrecto o expirado")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.CodeVerifiedError(e.message ?: "Error desconocido")
            }
        }
    }


    fun resetPassword(email: String, code: String, newPassword: String) {
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            try {
                val result = repository.resetPassword(email, code, newPassword)
                if (result.isSuccess) {
                    _forgotPasswordState.value = ForgotPasswordState.ResetPasswordSuccess("Contraseña restablecida")
                } else {
                    _forgotPasswordState.value = ForgotPasswordState.ResetPasswordError("No se pudo restablecer la contraseña")
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = ForgotPasswordState.ResetPasswordError(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
}

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class CodeSentSuccess(val message: String) : ForgotPasswordState()
    data class CodeVerifiedSuccess(val message: String) : ForgotPasswordState()
    data class ResetPasswordSuccess(val message: String) : ForgotPasswordState()
    data class CodeSentError(val message: String) : ForgotPasswordState()
    data class CodeVerifiedError(val message: String) : ForgotPasswordState()
    data class ResetPasswordError(val message: String) : ForgotPasswordState()
}
