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

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState

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

    fun updateClient(usuarioId: Int, updateData: Map<String, String?>) {
        _updateState.value = UpdateState.Updating
        viewModelScope.launch {
            try {
                val result = repository.updateClient(usuarioId, updateData)
                if (result.isSuccess) {
                    _updateState.value = UpdateState.UpdateSuccess(result.getOrThrow().message)
                } else {
                    _updateState.value = UpdateState.UpdateError(result.exceptionOrNull()?.message ?: "Error al actualizar cliente")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.UpdateError(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UpdateState.Idle
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

sealed class UpdateState {
    object Idle : UpdateState()
    object Updating : UpdateState()
    data class UpdateSuccess(val message: String) : UpdateState()
    data class UpdateError(val message: String) : UpdateState()
}
