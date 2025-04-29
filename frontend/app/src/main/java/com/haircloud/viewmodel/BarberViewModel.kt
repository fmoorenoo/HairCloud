package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.model.GetBarberResponse
import com.haircloud.data.model.BarberDate
import com.haircloud.data.repository.BarberRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BarberViewModel : ViewModel() {
    private val repository = BarberRepository()

    private val _barberState = MutableStateFlow<GetBarberState>(GetBarberState.Idle)
    val barberState: StateFlow<GetBarberState> = _barberState

    private val _barberDatesState = MutableStateFlow<BarberDatesState>(BarberDatesState.Idle)
    val barberDatesState: StateFlow<BarberDatesState> = _barberDatesState

    private val _barberUpdateState = MutableStateFlow<BarberUpdateState>(BarberUpdateState.Idle)
    val barberUpdateState: StateFlow<BarberUpdateState> = _barberUpdateState


    fun getBarber(userId: Int) {
        _barberState.value = GetBarberState.Loading
        viewModelScope.launch {
            try {
                val result = repository.getBarber(userId)
                if (result.isSuccess) {
                    _barberState.value = GetBarberState.Success(result.getOrThrow())
                } else {
                    _barberState.value = GetBarberState.Error(result.exceptionOrNull()?.message ?: "Error al obtener barbero")
                }
            } catch (e: Exception) {
                _barberState.value = GetBarberState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun getBarberDates(barberId: Int, date: String) {
        _barberDatesState.value = BarberDatesState.Loading
        viewModelScope.launch {
            val result = repository.getBarberDates(barberId, date)
            _barberDatesState.value = if (result.isSuccess) {
                BarberDatesState.Success(result.getOrThrow())
            } else {
                BarberDatesState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun updateBarber(usuarioId: Int, updateData: Map<String, String?>) {
        _barberUpdateState.value = BarberUpdateState.Updating
        viewModelScope.launch {
            try {
                val result = repository.updateBarber(usuarioId, updateData)
                if (result.isSuccess) {
                    _barberUpdateState.value = BarberUpdateState.UpdateSuccess(result.getOrThrow().message)
                } else {
                    _barberUpdateState.value = BarberUpdateState.UpdateError(result.exceptionOrNull()?.message ?: "Error al actualizar barbero")
                }
            } catch (e: Exception) {
                _barberUpdateState.value = BarberUpdateState.UpdateError(e.message ?: "Error desconocido")
            }
        }
    }

    fun resetUpdateState() {
        _barberUpdateState.value = BarberUpdateState.Idle
    }

}

sealed class GetBarberState {
    object Idle : GetBarberState()
    object Loading : GetBarberState()
    data class Success(val barber: GetBarberResponse) : GetBarberState()
    data class Error(val message: String) : GetBarberState()
}

sealed class BarberDatesState {
    object Idle : BarberDatesState()
    object Loading : BarberDatesState()
    data class Success(val citas: List<BarberDate>) : BarberDatesState()
    data class Error(val message: String) : BarberDatesState()
}

sealed class BarberUpdateState {
    object Idle : BarberUpdateState()
    object Updating : BarberUpdateState()
    data class UpdateSuccess(val message: String) : BarberUpdateState()
    data class UpdateError(val message: String) : BarberUpdateState()
}



