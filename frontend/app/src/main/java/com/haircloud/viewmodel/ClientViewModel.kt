package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.model.ClientResponse
import com.haircloud.data.repository.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ClientViewModel : ViewModel() {
    private val repository = ClientRepository()

    private val _clientState = MutableStateFlow<ClientState>(ClientState.Idle)
    val clientState: StateFlow<ClientState> = _clientState

    fun getClient(usuarioId: Int) {
        _clientState.value = ClientState.Loading
        viewModelScope.launch {
            try {
                val result = repository.getClient(usuarioId)
                if (result.isSuccess) {
                    _clientState.value = ClientState.Success(result.getOrThrow())
                } else {
                    _clientState.value = ClientState.Error(result.exceptionOrNull()?.message ?: "No se pudo obtener el cliente")
                }
            } catch (e: Exception) {
                _clientState.value = ClientState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetClientState() {
        _clientState.value = ClientState.Idle
    }
}

sealed class ClientState {
    object Idle : ClientState()
    object Loading : ClientState()
    data class Success(val client: ClientResponse) : ClientState()
    data class Error(val message: String) : ClientState()
}
