package com.haircloud.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haircloud.data.model.BarberResponse
import com.haircloud.data.model.BarbershopResponse
import com.haircloud.data.model.ReviewResponse
import com.haircloud.data.model.ServiceResponse
import com.haircloud.data.repository.BarbershopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BarbershopViewModel : ViewModel() {
    private val repository = BarbershopRepository()

    private val _barbershopState = MutableStateFlow<BarbershopState>(BarbershopState.Idle)
    val barbershopState: StateFlow<BarbershopState> = _barbershopState

    private val _barbershopUpdateState = MutableStateFlow<BarbershopUpdateState>(BarbershopUpdateState.Idle)
    val barbershopUpdateState: StateFlow<BarbershopUpdateState> = _barbershopUpdateState

    private val _singleBarbershopState = MutableStateFlow<SingleBarbershopState>(SingleBarbershopState.Idle)
    val singleBarbershopState: StateFlow<SingleBarbershopState> = _singleBarbershopState

    private val _servicesState = MutableStateFlow<ServiceState>(ServiceState.Idle)
    val servicesState: StateFlow<ServiceState> = _servicesState

    private val _singleServiceState = MutableStateFlow<SingleServiceState>(SingleServiceState.Idle)
    val singleServiceState: StateFlow<SingleServiceState> = _singleServiceState

    private val _reviewsState = MutableStateFlow<ReviewsState>(ReviewsState.Idle)
    val reviewsState: StateFlow<ReviewsState> = _reviewsState

    private val _deleteReviewState = MutableStateFlow<DeleteReviewState>(DeleteReviewState.Idle)
    val deleteReviewState: StateFlow<DeleteReviewState> = _deleteReviewState

    private val _addReviewState = MutableStateFlow<AddReviewState>(AddReviewState.Idle)
    val addReviewState: StateFlow<AddReviewState> = _addReviewState

    private val _barbersState = MutableStateFlow<BarbersState>(BarbersState.Idle)
    val barbersState: StateFlow<BarbersState> = _barbersState


    fun getAllBarbershops(clienteId: Int) {
        _barbershopState.value = BarbershopState.Loading
        viewModelScope.launch {
            val result = repository.getAllBarbershops(clienteId)
            _barbershopState.value = result.fold(
                onSuccess = { BarbershopState.Success(it) },
                onFailure = { BarbershopState.Error(it.message ?: "Error al cargar barberías") }
            )
        }
    }

    fun updateBarbershop(localId: Int, updateData: Map<String, String?>) {
        _barbershopUpdateState.value = BarbershopUpdateState.Updating
        viewModelScope.launch {
            val result = repository.updateBarbershop(localId, updateData)
            _barbershopUpdateState.value = result.fold(
                onSuccess = { BarbershopUpdateState.UpdateSuccess(it) },
                onFailure = { BarbershopUpdateState.UpdateError(it.message ?: "Error al actualizar") }
            )
        }
    }

    fun getBarbershopById(clienteId: Int, localId: Int) {
        _singleBarbershopState.value = SingleBarbershopState.Loading
        viewModelScope.launch {
            val result = repository.getBarbershopById(clienteId, localId)
            _singleBarbershopState.value = result.fold(
                onSuccess = { SingleBarbershopState.Success(it) },
                onFailure = { SingleBarbershopState.Error(it.message ?: "Error al cargar la barbería") }
            )
        }
    }

    fun getFavoriteBarbershops(clienteId: Int) {
        _barbershopState.value = BarbershopState.Loading
        viewModelScope.launch {
            val result = repository.getFavoriteBarbershops(clienteId)
            _barbershopState.value = result.fold(
                onSuccess = { BarbershopState.Success(it) },
                onFailure = { BarbershopState.Error(it.message ?: "Error al cargar favoritos") }
            )
        }
    }

    fun addFavorite(clienteId: Int, localId: Int, show: String = "All") {
        viewModelScope.launch {
            repository.addFavorite(clienteId, localId)
            when (show) {
                "Favorites" -> getFavoriteBarbershops(clienteId)
                "All" -> getAllBarbershops(clienteId)
                "One" -> getBarbershopById(clienteId, localId)
            }
        }
    }

    fun removeFavorite(clienteId: Int, localId: Int, show: String = "All") {
        viewModelScope.launch {
            repository.removeFavorite(clienteId, localId)
            when (show) {
                "Favorites" -> getFavoriteBarbershops(clienteId)
                "All" -> getAllBarbershops(clienteId)
                "One" -> getBarbershopById(clienteId, localId)
            }
        }
    }

    fun getServicesById(localId: Int) {
        _servicesState.value = ServiceState.Loading
        viewModelScope.launch {
            val result = repository.getServicesById(localId)
            _servicesState.value = result.fold(
                onSuccess = { ServiceState.Success(it) },
                onFailure = { ServiceState.Error(it.message ?: "Error al cargar servicios") }
            )
        }
    }

    fun getService(servicioId: Int) {
        _singleServiceState.value = SingleServiceState.Loading
        viewModelScope.launch {
            val result = repository.getService(servicioId)
            _singleServiceState.value = result.fold(
                onSuccess = { SingleServiceState.Success(it) },
                onFailure = { SingleServiceState.Error(it.message ?: "Error al cargar el servicio") }
            )
        }
    }

    fun getBarbershopReviews(localId: Int) {
        _reviewsState.value = ReviewsState.Loading
        viewModelScope.launch {
            val result = repository.getBarbershopReviews(localId)
            _reviewsState.value = result.fold(
                onSuccess = { ReviewsState.Success(it) },
                onFailure = { ReviewsState.Error(it.message ?: "Error al cargar reseñas") }
            )
        }
    }

    fun addReview(clienteId: Int, localId: Int, calificacion: Double, comentario: String) {
        _addReviewState.value = AddReviewState.Loading
        viewModelScope.launch {
            val result = repository.addReview(clienteId, localId, calificacion, comentario)
            result.fold(
                onSuccess = {
                    _addReviewState.value = AddReviewState.Success
                    getBarbershopReviews(localId)
                    getBarbershopById(clienteId, localId)
                },
                onFailure = {
                    _addReviewState.value = AddReviewState.Error(it.message ?: "Error al añadir reseña")
                }
            )
        }
    }

    fun resetAddReviewState() {
        _addReviewState.value = AddReviewState.Idle
    }

    fun resetUpdateState() {
        _barbershopUpdateState.value = BarbershopUpdateState.Idle
    }

    fun deleteReview(resenaId: Int, localId: Int, clienteId: Int) {
        _deleteReviewState.value = DeleteReviewState.Idle
        viewModelScope.launch {
            val result = repository.deleteReview(resenaId)
            result.fold(
                onSuccess = {
                    _deleteReviewState.value = DeleteReviewState.Success
                    getBarbershopReviews(localId)
                    getBarbershopById(clienteId, localId)
                },
                onFailure = {
                    _deleteReviewState.value = DeleteReviewState.Error(it.message ?: "Error al eliminar reseña")
                }
            )
        }
    }

    fun resetDeleteReviewState() {
        _deleteReviewState.value = DeleteReviewState.Idle
    }

    fun getBarbersByLocalId(localId: Int) {
        _barbersState.value = BarbersState.Loading
        viewModelScope.launch {
            val result = repository.getBarbersById(localId)
            _barbersState.value = result.fold(
                onSuccess = { BarbersState.Success(it) },
                onFailure = { BarbersState.Error(it.message ?: "Error al cargar peluqueros") }
            )
        }
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

sealed class ServiceState {
    object Idle : ServiceState()
    object Loading : ServiceState()
    data class Success(val services: List<ServiceResponse>) : ServiceState()
    data class Error(val message: String) : ServiceState()
}

sealed class SingleServiceState {
    object Idle : SingleServiceState()
    object Loading : SingleServiceState()
    data class Success(val service: ServiceResponse) : SingleServiceState()
    data class Error(val message: String) : SingleServiceState()
}

sealed class ReviewsState {
    object Idle : ReviewsState()
    object Loading : ReviewsState()
    data class Success(val reviews: List<ReviewResponse>) : ReviewsState()
    data class Error(val message: String) : ReviewsState()
}

sealed class DeleteReviewState {
    object Idle : DeleteReviewState()
    object Success : DeleteReviewState()
    data class Error(val message: String) : DeleteReviewState()
}

sealed class AddReviewState {
    object Idle : AddReviewState()
    object Loading : AddReviewState()
    object Success : AddReviewState()
    data class Error(val message: String) : AddReviewState()
}

sealed class BarbersState {
    object Idle : BarbersState()
    object Loading : BarbersState()
    data class Success(val barbers: List<BarberResponse>) : BarbersState()
    data class Error(val message: String) : BarbersState()
}

sealed class BarbershopUpdateState {
    object Idle : BarbershopUpdateState()
    object Updating : BarbershopUpdateState()
    data class UpdateSuccess(val message: String) : BarbershopUpdateState()
    data class UpdateError(val message: String) : BarbershopUpdateState()
}

