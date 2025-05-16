package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.ApiResponse
import com.haircloud.data.model.AddDateRequest
import com.haircloud.data.repository.MailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MailViewModel : ViewModel() {
    private val repository = MailRepository()

    private val _mailState = MutableStateFlow<MailState>(MailState.Idle)
    val mailState: StateFlow<MailState> = _mailState

    fun sendInfoDate(request: AddDateRequest) {
        _mailState.value = MailState.Loading
        viewModelScope.launch {
            try {
                val result = repository.sendInfoDate(request)
                if (result.isSuccess) {
                    _mailState.value = MailState.Success(result.getOrThrow())
                } else {
                    _mailState.value = MailState.Error(result.exceptionOrNull()?.message ?: "No se pudo enviar el email")
                }
            } catch (e: Exception) {
                _mailState.value = MailState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetMailState() {
        _mailState.value = MailState.Idle
    }
}

sealed class MailState {
    object Idle : MailState()
    object Loading : MailState()
    data class Success(val response: ApiResponse) : MailState()
    data class Error(val message: String) : MailState()
}
