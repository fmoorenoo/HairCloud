package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.model.BarbershopResponse
import com.haircloud.data.repository.BarbershopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BarbershopViewModel : ViewModel() {
    private val repository = BarbershopRepository()

    private val _barbershopState = MutableStateFlow<BarbershopState>(BarbershopState.Idle)
    val barbershopState: StateFlow<BarbershopState> = _barbershopState

    fun getBarbershops(clienteId: Int) {
        _barbershopState.value = BarbershopState.Loading
        viewModelScope.launch {
            try {
                val result = repository.getBarbershops(clienteId)
                if (result.isSuccess) {
                    _barbershopState.value = BarbershopState.Success(result.getOrThrow())
                } else {
                    _barbershopState.value = BarbershopState.Error(result.exceptionOrNull()?.message ?: "No se pudieron obtener las barberías")
                }
            } catch (e: Exception) {
                _barbershopState.value = BarbershopState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun addFavorite(clienteId: Int, localId: Int) {
        viewModelScope.launch {
            repository.addFavorite(clienteId, localId)
            getBarbershops(clienteId)
        }
    }

    fun removeFavorite(clienteId: Int, localId: Int) {
        viewModelScope.launch {
            repository.removeFavorite(clienteId, localId)
            getBarbershops(clienteId)
        }
    }


    fun resetBarbershopState() {
        _barbershopState.value = BarbershopState.Idle
    }
}

sealed class BarbershopState {
    object Idle : BarbershopState()
    object Loading : BarbershopState()
    data class Success(val barbershops: List<BarbershopResponse>) : BarbershopState()
    data class Error(val message: String) : BarbershopState()
}


