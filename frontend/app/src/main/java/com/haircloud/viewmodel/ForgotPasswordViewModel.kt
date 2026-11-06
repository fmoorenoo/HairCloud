package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Envío y verificación de códigos por email (registro y recuperación de contraseña). */
class ForgotPasswordViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _forgotPasswordState = MutableStateFlow<ForgotPasswordState>(ForgotPasswordState.Idle)
    val forgotPasswordState: StateFlow<ForgotPasswordState> = _forgotPasswordState

    fun sendVerificationCode(email: String, purpose: String) {
        if (_forgotPasswordState.value is ForgotPasswordState.Loading) return
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            _forgotPasswordState.value = repository.sendVerificationCode(email, purpose).fold(
                // El servidor no revela si el email existe: el nombre de usuario
                // solo se conoce tras verificar el código.
                onSuccess = { ForgotPasswordState.CodeSentSuccess(it.message, null) },
                onFailure = { ForgotPasswordState.CodeSentError(it.message ?: "Error desconocido") }
            )
        }
    }

    fun verifyCode(email: String, code: String, purpose: String) {
        if (_forgotPasswordState.value is ForgotPasswordState.Loading) return
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            _forgotPasswordState.value = repository.verifyCode(email, code, purpose).fold(
                onSuccess = {
                    ForgotPasswordState.CodeVerifiedSuccess(it.message, it.verification_token, it.username)
                },
                onFailure = { ForgotPasswordState.CodeVerifiedError(it.message ?: "Código incorrecto o caducado") }
            )
        }
    }

    /**
     * @param resetToken token devuelto por [verifyCode] (de un solo uso). El
     * parámetro [email] se conserva por compatibilidad con la pantalla.
     */
    @Suppress("UNUSED_PARAMETER")
    fun resetPassword(email: String, resetToken: String, newPassword: String) {
        if (_forgotPasswordState.value is ForgotPasswordState.Loading) return
        _forgotPasswordState.value = ForgotPasswordState.Loading
        viewModelScope.launch {
            _forgotPasswordState.value = repository.resetPassword(resetToken, newPassword).fold(
                onSuccess = { ForgotPasswordState.ResetPasswordSuccess("Contraseña restablecida") },
                onFailure = { ForgotPasswordState.ResetPasswordError(it.message ?: "No se pudo restablecer la contraseña") }
            )
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordState.Idle
    }
}

sealed class ForgotPasswordState {
    object Idle : ForgotPasswordState()
    object Loading : ForgotPasswordState()
    data class CodeSentSuccess(val message: String, val username: String?) : ForgotPasswordState()
    data class CodeVerifiedSuccess(
        val message: String,
        val verificationToken: String,
        val username: String?
    ) : ForgotPasswordState()
    data class ResetPasswordSuccess(val message: String) : ForgotPasswordState()
    data class CodeSentError(val message: String) : ForgotPasswordState()
    data class CodeVerifiedError(val message: String) : ForgotPasswordState()
    data class ResetPasswordError(val message: String) : ForgotPasswordState()
}
