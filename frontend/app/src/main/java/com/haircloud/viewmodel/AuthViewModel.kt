package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.SessionManager
import com.haircloud.data.model.LoginResponse
import com.haircloud.data.model.RegisterResponse
import com.haircloud.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val tokenManager = SessionManager.store

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun login(username: String, password: String) {
        if (_loginState.value is LoginState.Loading) return
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            _loginState.value = repository.login(username, password).fold(
                onSuccess = { LoginState.Success(it) },
                onFailure = { LoginState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    /**
     * @param verificationToken token obtenido al verificar el código enviado al email;
     * el servidor rechaza registros sin email verificado.
     */
    fun register(nombre: String, email: String, username: String, password: String, verificationToken: String?) {
        if (_registerState.value is RegisterState.Loading) return
        if (verificationToken.isNullOrBlank()) {
            _registerState.value = RegisterState.Error("Verifica tu correo antes de continuar")
            return
        }
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            _registerState.value = repository.register(nombre, email, username, password, verificationToken).fold(
                onSuccess = { RegisterState.Success(it) },
                onFailure = { RegisterState.Error(it.message ?: "No se pudo registrar el usuario") }
            )
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }

    fun getTokenFlow() = tokenManager.token
    fun getUserIdFlow() = tokenManager.userId
    fun getRoleFlow() = tokenManager.role

    /** Revoca la sesión en el servidor y borra los tokens locales. */
    fun logout() {
        // No se usa viewModelScope: la pantalla se destruye al navegar al login
        // y cancelaría la operación antes de terminar.
        SessionManager.appScope.launch {
            repository.logout()
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val response: LoginResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val response: RegisterResponse) : RegisterState()
    data class Error(val message: String) : RegisterState()
}
