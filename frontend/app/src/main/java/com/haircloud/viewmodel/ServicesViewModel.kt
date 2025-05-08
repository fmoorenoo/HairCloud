package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.model.ServiceRequest
import com.haircloud.data.model.ServiceResponse
import com.haircloud.data.repository.ServicesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServicesViewModel : ViewModel() {
    private val repository = ServicesRepository()

    private val _servicesState = MutableStateFlow<BarberServiceState>(BarberServiceState.Idle)
    val servicesState: StateFlow<BarberServiceState> = _servicesState

    private val _singleServiceState = MutableStateFlow<BarberSingleServiceState>(BarberSingleServiceState.Idle)
    val singleServiceState: StateFlow<BarberSingleServiceState> = _singleServiceState

    private val _createState = MutableStateFlow<BarberServiceOperationState>(BarberServiceOperationState.Idle)
    val createState: StateFlow<BarberServiceOperationState> = _createState

    private val _editState = MutableStateFlow<BarberServiceOperationState>(BarberServiceOperationState.Idle)
    val editState: StateFlow<BarberServiceOperationState> = _editState

    private val _deleteState = MutableStateFlow<BarberServiceOperationState>(BarberServiceOperationState.Idle)
    val deleteState: StateFlow<BarberServiceOperationState> = _deleteState

    fun getServicesByLocalId(localId: Int) {
        _servicesState.value = BarberServiceState.Loading
        viewModelScope.launch {
            val result = repository.getServicesByLocalId(localId)
            _servicesState.value = result.fold(
                onSuccess = { BarberServiceState.Success(it) },
                onFailure = { BarberServiceState.Error(it.message ?: "Error al obtener servicios") }
            )
        }
    }

    fun getService(servicioId: Int) {
        _singleServiceState.value = BarberSingleServiceState.Loading
        viewModelScope.launch {
            val result = repository.getService(servicioId)
            _singleServiceState.value = result.fold(
                onSuccess = { BarberSingleServiceState.Success(it) },
                onFailure = { BarberSingleServiceState.Error(it.message ?: "Error al obtener el servicio") }
            )
        }
    }

    fun createService(service: ServiceRequest) {
        _createState.value = BarberServiceOperationState.Loading
        viewModelScope.launch {
            val result = repository.createService(service)
            _createState.value = result.fold(
                onSuccess = { BarberServiceOperationState.Success(it) },
                onFailure = { BarberServiceOperationState.Error(it.message ?: "Error al crear el servicio") }
            )
        }
    }

    fun editService(servicioId: Int, service: ServiceRequest) {
        _editState.value = BarberServiceOperationState.Loading
        viewModelScope.launch {
            val result = repository.editService(servicioId, service)
            _editState.value = result.fold(
                onSuccess = { BarberServiceOperationState.Success(it) },
                onFailure = { BarberServiceOperationState.Error(it.message ?: "Error al editar el servicio") }
            )
        }
    }

    fun deleteService(servicioId: Int) {
        _deleteState.value = BarberServiceOperationState.Loading
        viewModelScope.launch {
            val result = repository.deleteService(servicioId)
            _deleteState.value = result.fold(
                onSuccess = { BarberServiceOperationState.Success(it) },
                onFailure = { BarberServiceOperationState.Error(it.message ?: "Error al eliminar el servicio") }
            )
        }
    }

    fun resetCreateState() {
        _createState.value = BarberServiceOperationState.Idle
    }

    fun resetEditState() {
        _editState.value = BarberServiceOperationState.Idle
    }

    fun resetDeleteState() {
        _deleteState.value = BarberServiceOperationState.Idle
    }
}

sealed class BarberServiceState {
    object Idle : BarberServiceState()
    object Loading : BarberServiceState()
    data class Success(val services: List<ServiceResponse>) : BarberServiceState()
    data class Error(val message: String) : BarberServiceState()
}

sealed class BarberSingleServiceState {
    object Idle : BarberSingleServiceState()
    object Loading : BarberSingleServiceState()
    data class Success(val service: ServiceResponse) : BarberSingleServiceState()
    data class Error(val message: String) : BarberSingleServiceState()
}

sealed class BarberServiceOperationState {
    object Idle : BarberServiceOperationState()
    object Loading : BarberServiceOperationState()
    data class Success(val message: String) : BarberServiceOperationState()
    data class Error(val message: String) : BarberServiceOperationState()
}
