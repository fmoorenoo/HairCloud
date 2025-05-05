package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.model.AddDateRequest
import com.haircloud.data.repository.DatesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DatesViewModel : ViewModel() {
    private val repository = DatesRepository()

    private val _addDateState = MutableStateFlow<DateOperationState>(DateOperationState.Idle)
    val addDateState: StateFlow<DateOperationState> = _addDateState

    private val _deleteDateState = MutableStateFlow<DateOperationState>(DateOperationState.Idle)
    val deleteDateState: StateFlow<DateOperationState> = _deleteDateState

    private val _updateEstadoState = MutableStateFlow<DateOperationState>(DateOperationState.Idle)
    val updateEstadoState: StateFlow<DateOperationState> = _updateEstadoState

    fun addDate(request: AddDateRequest) {
        _addDateState.value = DateOperationState.Loading
        viewModelScope.launch {
            val result = repository.addDate(request)
            _addDateState.value = result.fold(
                onSuccess = { DateOperationState.Success(it.message) },
                onFailure = { DateOperationState.Error(it.message ?: "Error al añadir la cita") }
            )
        }
    }

    fun deleteDate(citaId: Int) {
        _deleteDateState.value = DateOperationState.Loading
        viewModelScope.launch {
            val result = repository.deleteDate(citaId)
            _deleteDateState.value = result.fold(
                onSuccess = { DateOperationState.Success(it.message) },
                onFailure = { DateOperationState.Error(it.message ?: "Error al eliminar la cita") }
            )
        }
    }

    fun updateDateEstado(citaId: Int, estado: String) {
        _updateEstadoState.value = DateOperationState.Loading
        viewModelScope.launch {
            val result = repository.updateDateEstado(citaId, estado)
            _updateEstadoState.value = result.fold(
                onSuccess = { DateOperationState.Success(it.message) },
                onFailure = { DateOperationState.Error(it.message ?: "Error al actualizar estado") }
            )
        }
    }

    fun resetUpdateEstadoState() {
        _updateEstadoState.value = DateOperationState.Idle
    }


    fun resetAddDateState() {
        _addDateState.value = DateOperationState.Idle
    }

    fun resetDeleteDateState() {
        _deleteDateState.value = DateOperationState.Idle
    }
}

sealed class DateOperationState {
    object Idle : DateOperationState()
    object Loading : DateOperationState()
    data class Success(val message: String) : DateOperationState()
    data class Error(val message: String) : DateOperationState()
}