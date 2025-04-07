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

    private val _singleBarbershopState = MutableStateFlow<SingleBarbershopState>(SingleBarbershopState.Idle)
    val singleBarbershopState: StateFlow<SingleBarbershopState> = _singleBarbershopState


    fun getAllBarbershops(clienteId: Int) {
        _barbershopState.value = BarbershopState.Loading
        viewModelScope.launch {
            try {
                val result = repository.getAllBarbershops(clienteId)
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

    fun getBarbershopById(clienteId: Int, localId: Int) {
        _singleBarbershopState.value = SingleBarbershopState.Loading
        viewModelScope.launch {
            try {
                val result = repository.getBarbershopById(clienteId, localId)
                if (result.isSuccess) {
                    _singleBarbershopState.value = SingleBarbershopState.Success(result.getOrThrow())
                } else {
                    _singleBarbershopState.value = SingleBarbershopState.Error(result.exceptionOrNull()?.message ?: "No se pudo obtener la barbería")
                }
            } catch (e: Exception) {
                _singleBarbershopState.value = SingleBarbershopState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getFavoriteBarbershops(clienteId: Int) {
        _barbershopState.value = BarbershopState.Loading
        viewModelScope.launch {
            try {
                val result = repository.getFavoriteBarbershops(clienteId)
                if (result.isSuccess) {
                    _barbershopState.value = BarbershopState.Success(result.getOrThrow())
                } else {
                    _barbershopState.value = BarbershopState.Error(result.exceptionOrNull()?.message ?: "No se pudieron obtener las barberías favoritas")
                }
            } catch (e: Exception) {
                _barbershopState.value = BarbershopState.Error(e.message ?: "Error desconocido")
            }
        }
    }


    fun addFavorite(clienteId: Int, localId: Int, show: String = "All") {
        viewModelScope.launch {
            repository.addFavorite(clienteId, localId)
            if (show == "Favorites") {
                getFavoriteBarbershops(clienteId)
            } else if (show == "All") {
                getAllBarbershops(clienteId)
            } else if (show == "One") {
                getBarbershopById(clienteId, localId)
            }
        }
    }

    fun removeFavorite(clienteId: Int, localId: Int, show: String = "All") {
        viewModelScope.launch {
            repository.removeFavorite(clienteId, localId)
            if (show == "Favorites") {
                getFavoriteBarbershops(clienteId)
            } else if (show == "All") {
                getAllBarbershops(clienteId)
            } else if (show == "One") {
                getBarbershopById(clienteId, localId)
            }
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

sealed class SingleBarbershopState {
    object Idle : SingleBarbershopState()
    object Loading : SingleBarbershopState()
    data class Success(val barbershop: BarbershopResponse) : SingleBarbershopState()
    data class Error(val message: String) : SingleBarbershopState()
}


