package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.model.BarberActivityResponse
import com.haircloud.data.model.GetBarberResponse
import com.haircloud.data.model.BarberDate
import com.haircloud.data.model.BarberStatsResponse
import com.haircloud.data.model.CreateBarberRequest
import com.haircloud.data.model.InactiveBarberResponse
import com.haircloud.data.model.WorkDaySchedule
import com.haircloud.data.repository.BarberRepository
import com.haircloud.screens.barber.WorkSchedule
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

    private val _inactiveBarbersState = MutableStateFlow<InactiveBarbersState>(InactiveBarbersState.Idle)
    val inactiveBarbersState: StateFlow<InactiveBarbersState> = _inactiveBarbersState

    private val _createBarberState = MutableStateFlow<CreateBarberState>(CreateBarberState.Idle)
    val createBarberState: StateFlow<CreateBarberState> = _createBarberState

    private val _barberActivityState = MutableStateFlow<BarberActivityState>(BarberActivityState.Idle)
    val barberActivityState: StateFlow<BarberActivityState> = _barberActivityState

    private val _barberStatsState = MutableStateFlow<BarberStatsState>(BarberStatsState.Idle)
    val barberStatsState: StateFlow<BarberStatsState> = _barberStatsState

    private val _barberStatsEmailState = MutableStateFlow<BarberStatsEmailState>(BarberStatsEmailState.Idle)
    val barberStatsEmailState: StateFlow<BarberStatsEmailState> = _barberStatsEmailState

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

    fun getBarberDatesInRange(barberId: Int, startDate: String, endDate: String) {
        _barberDatesState.value = BarberDatesState.Loading
        viewModelScope.launch {
            val result = repository.getBarberDatesInRange(barberId, startDate, endDate)
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

    fun toggleBarberRole(usuarioId: Int) {
        _barberUpdateState.value = BarberUpdateState.Updating
        viewModelScope.launch {
            val result = repository.toggleBarberRole(usuarioId)
            _barberUpdateState.value = if (result.isSuccess) {
                BarberUpdateState.UpdateSuccess(result.getOrThrow().message)
            } else {
                BarberUpdateState.UpdateError(result.exceptionOrNull()?.message ?: "Error al cambiar rol")
            }
        }
    }

    fun deactivateBarber(usuarioId: Int) {
        _barberUpdateState.value = BarberUpdateState.Updating
        viewModelScope.launch {
            val result = repository.deactivateBarber(usuarioId)
            _barberUpdateState.value = if (result.isSuccess) {
                BarberUpdateState.UpdateSuccess(result.getOrThrow().message)
            } else {
                BarberUpdateState.UpdateError(result.exceptionOrNull()?.message ?: "Error al desactivar barbero")
            }
        }
    }

    fun activateBarber(usuarioId: Int) {
        _barberUpdateState.value = BarberUpdateState.Updating
        viewModelScope.launch {
            val result = repository.activateBarber(usuarioId)
            _barberUpdateState.value = if (result.isSuccess) {
                BarberUpdateState.UpdateSuccess(result.getOrThrow().message)
            } else {
                BarberUpdateState.UpdateError(result.exceptionOrNull()?.message ?: "Error al activar barbero")
            }
        }
    }

    fun getInactiveBarbers() {
        _inactiveBarbersState.value = InactiveBarbersState.Loading
        viewModelScope.launch {
            val result = repository.getInactiveBarbers()
            _inactiveBarbersState.value = if (result.isSuccess) {
                InactiveBarbersState.Success(result.getOrThrow())
            } else {
                InactiveBarbersState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun createBarber(request: CreateBarberRequest) {
        _createBarberState.value = CreateBarberState.Creating
        viewModelScope.launch {
            val result = repository.createBarber(request)
            _createBarberState.value = if (result.isSuccess) {
                CreateBarberState.Success(result.getOrThrow().message)
            } else {
                CreateBarberState.Error(result.exceptionOrNull()?.message ?: "Error al crear barbero")
            }
        }
    }

    fun resetCreateBarberState() {
        _createBarberState.value = CreateBarberState.Idle
    }


    fun updateBarberSchedule(peluqueroId: Int, schedule: List<WorkSchedule>) {
        _barberUpdateState.value = BarberUpdateState.Updating
        viewModelScope.launch {
            val formattedSchedule = schedule.map {
                WorkDaySchedule(
                    dia = it.diaSemana,
                    inicio = it.horaInicio,
                    fin = it.horaFin
                )
            }

            val result = repository.updateBarberSchedule(peluqueroId, formattedSchedule)
            _barberUpdateState.value = if (result.isSuccess) {
                BarberUpdateState.UpdateSuccess(result.getOrThrow().message)
            } else {
                BarberUpdateState.UpdateError(result.exceptionOrNull()?.message ?: "Error al actualizar horario")
            }
        }
    }

    fun getBarberActivity(peluqueroId: Int) {
        _barberActivityState.value = BarberActivityState.Loading
        viewModelScope.launch {
            val result = repository.getBarberActivity(peluqueroId)
            _barberActivityState.value = if (result.isSuccess) {
                BarberActivityState.Success(result.getOrThrow())
            } else {
                BarberActivityState.Error(result.exceptionOrNull()?.message ?: "Error al cargar actividad")
            }
        }
    }

    fun getBarberStats(peluqueroId: Int, localId: Int, start: String, end: String) {
        _barberStatsState.value = BarberStatsState.Loading
        viewModelScope.launch {
            val result = repository.getBarberStats(peluqueroId, localId, start, end)
            _barberStatsState.value = if (result.isSuccess) {
                BarberStatsState.Success(result.getOrThrow())
            } else {
                BarberStatsState.Error(result.exceptionOrNull()?.message ?: "Error al obtener estadísticas")
            }
        }
    }

    fun sendBarberStatsEmail(
        peluqueroId: Int,
        stats: BarberStatsResponse,
        startDate: String,
        endDate: String
    ) {
        _barberStatsEmailState.value = BarberStatsEmailState.Sending
        viewModelScope.launch {
            val result = repository.sendBarberStatsEmail(peluqueroId, stats, startDate, endDate)
            _barberStatsEmailState.value = if (result.isSuccess) {
                BarberStatsEmailState.Success(result.getOrThrow().message)
            } else {
                BarberStatsEmailState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun resetBarberStatsEmailState() {
        _barberStatsEmailState.value = BarberStatsEmailState.Idle
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

sealed class InactiveBarbersState {
    object Idle : InactiveBarbersState()
    object Loading : InactiveBarbersState()
    data class Success(val barbers: List<InactiveBarberResponse>) : InactiveBarbersState()
    data class Error(val message: String) : InactiveBarbersState()
}

sealed class CreateBarberState {
    object Idle : CreateBarberState()
    object Creating : CreateBarberState()
    data class Success(val message: String) : CreateBarberState()
    data class Error(val message: String) : CreateBarberState()
}

sealed class BarberActivityState {
    object Idle : BarberActivityState()
    object Loading : BarberActivityState()
    data class Success(val actividades: List<BarberActivityResponse>) : BarberActivityState()
    data class Error(val message: String) : BarberActivityState()
}

sealed class BarberStatsState {
    object Idle : BarberStatsState()
    object Loading : BarberStatsState()
    data class Success(val stats: BarberStatsResponse) : BarberStatsState()
    data class Error(val message: String) : BarberStatsState()
}

sealed class BarberStatsEmailState {
    object Idle : BarberStatsEmailState()
    object Sending : BarberStatsEmailState()
    data class Success(val message: String) : BarberStatsEmailState()
    data class Error(val message: String) : BarberStatsEmailState()
}