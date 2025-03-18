package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.haircloud.data.LoginResponse
import com.haircloud.data.RegisterResponse
import com.haircloud.data.UserRepository

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState

    fun login(username: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val result = repository.login(username, password)
                if (result.isSuccess) {
                    _loginState.value = LoginState.Success(result.getOrThrow())
                } else {
                    _loginState.value = LoginState.Error("Usuario o contraseña incorrectos")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun register(nombre: String, email: String, username: String, password: String) {
        _registerState.value = RegisterState.Loading
        viewModelScope.launch {
            try {
                val result = repository.register(nombre, email, username, password)
                if (result.isSuccess) {
                    _registerState.value = RegisterState.Success(result.getOrThrow())
                } else {
                    _registerState.value = RegisterState.Error("No se pudo registrar el usuario")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
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
