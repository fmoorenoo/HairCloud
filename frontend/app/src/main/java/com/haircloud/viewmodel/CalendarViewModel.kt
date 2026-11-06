package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.model.*
import com.haircloud.data.repository.CalendarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CalendarViewModel : ViewModel() {
    private val repository = CalendarRepository()

    private val _weeklyScheduleState = MutableStateFlow<WeeklyScheduleState>(WeeklyScheduleState.Idle)
    val weeklyScheduleState: StateFlow<WeeklyScheduleState> = _weeklyScheduleState

    private val _blockedHoursState = MutableStateFlow<BlockedHoursState>(BlockedHoursState.Idle)
    val blockedHoursState: StateFlow<BlockedHoursState> = _blockedHoursState

    private val _availableSlotsState = MutableStateFlow<AvailableSlotsState>(AvailableSlotsState.Idle)
    val availableSlotsState: StateFlow<AvailableSlotsState> = _availableSlotsState


    fun getWeeklySchedule(peluqueroId: Int) {
        _weeklyScheduleState.value = WeeklyScheduleState.Loading
        viewModelScope.launch {
            val result = repository.getWeeklyCalendar(peluqueroId)
            _weeklyScheduleState.value = result.fold(
                onSuccess = { WeeklyScheduleState.Success(it) },
                onFailure = { WeeklyScheduleState.Error(it.message ?: "Error al cargar el calendario semanal") }
            )
        }
    }

    fun getBlockedHours(peluqueroId: Int) {
        _blockedHoursState.value = BlockedHoursState.Loading
        viewModelScope.launch {
            val result = repository.getBlockedHours(peluqueroId)
            _blockedHoursState.value = result.fold(
                onSuccess = { BlockedHoursState.Success(it) },
                onFailure = { BlockedHoursState.Error(it.message ?: "Error al cargar bloqueos") }
            )
        }
    }

    /** Huecos libres para un servicio: la duración la decide el servidor según el servicio. */
    fun getAvailableSlots(peluqueroId: Int, fecha: String, servicioId: Int) {
        if (servicioId <= 0) return
        _availableSlotsState.value = AvailableSlotsState.Loading
        viewModelScope.launch {
            val result = repository.getAvailableSlots(peluqueroId, fecha, servicioId)
            _availableSlotsState.value = result.fold(
                onSuccess = { AvailableSlotsState.Success(it) },
                onFailure = { AvailableSlotsState.Error(it.message ?: "Error al cargar huecos disponibles") }
            )
        }
    }
}

sealed class WeeklyScheduleState {
    object Idle : WeeklyScheduleState()
    object Loading : WeeklyScheduleState()
    data class Success(val schedule: List<WeeklyScheduleResponse>) : WeeklyScheduleState()
    data class Error(val message: String) : WeeklyScheduleState()
}

sealed class BlockedHoursState {
    object Idle : BlockedHoursState()
    object Loading : BlockedHoursState()
    data class Success(val blocked: List<BlockedHoursResponse>) : BlockedHoursState()
    data class Error(val message: String) : BlockedHoursState()
}

sealed class AvailableSlotsState {
    object Idle : AvailableSlotsState()
    object Loading : AvailableSlotsState()
    data class Success(val slots: List<AvailableSlot>) : AvailableSlotsState()
    data class Error(val message: String) : AvailableSlotsState()
}
