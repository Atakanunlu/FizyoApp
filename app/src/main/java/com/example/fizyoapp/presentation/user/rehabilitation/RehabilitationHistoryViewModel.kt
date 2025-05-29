package com.example.fizyoapp.presentation.user.rehabilitation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.appointment.Appointment
import com.example.fizyoapp.domain.model.appointment.AppointmentStatus
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.usecase.appointment.CancelAppointmentWithRoleUseCase
import com.example.fizyoapp.domain.usecase.appointment.GetUserAppointmentsUseCase
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

data class AppointmentWithPhysiotherapist(
    val appointment: Appointment,
    val physiotherapistName: String,
    val physiotherapistPhotoUrl: String
)

data class RehabilitationHistoryState(
    val pastAppointments: List<AppointmentWithPhysiotherapist> = emptyList(),
    val upcomingAppointments: List<AppointmentWithPhysiotherapist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

sealed class RehabilitationHistoryEvent {
    object Refresh : RehabilitationHistoryEvent()
    data class CancelAppointment(val appointmentId: String) : RehabilitationHistoryEvent()
}

@HiltViewModel
class RehabilitationHistoryViewModel @Inject constructor(
    private val getUserAppointmentsUseCase: GetUserAppointmentsUseCase,
    private val getPhysiotherapistByIdUseCase: GetPhysiotherapistByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val cancelAppointmentWithRoleUseCase: CancelAppointmentWithRoleUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(RehabilitationHistoryState())
    val state: StateFlow<RehabilitationHistoryState> = _state.asStateFlow()

    private var currentUserId: String? = null
    private var appointmentsObservationJob: Job? = null
    private var periodicRefreshJob: Job? = null
    private val firestore = FirebaseFirestore.getInstance()

    private val physiotherapistCache = mutableMapOf<String, PhysiotherapistProfile?>()

    init {
        loadCurrentUserAndStartObservingAppointments()
        startPeriodicRefresh()
    }

    private fun startPeriodicRefresh() {
        periodicRefreshJob = viewModelScope.launch {
            while (true) {
                delay(30000)
                currentUserId?.let { userId ->
                    try {
                        getUserAppointmentsUseCase(userId).collect { result ->
                            if (result is Resource.Success) {
                                processAppointments(result.data ?: emptyList())
                            }
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    fun onEvent(event: RehabilitationHistoryEvent) {
        when (event) {
            is RehabilitationHistoryEvent.Refresh -> {
                physiotherapistCache.clear()
                loadAppointments()
            }
            is RehabilitationHistoryEvent.CancelAppointment -> {
                cancelAppointment(event.appointmentId)
            }
        }
    }

    private fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                cancelAppointmentWithRoleUseCase(appointmentId, "user").collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                successMessage = "Randevu başarıyla iptal edildi"
                            )
                            currentUserId?.let { loadAppointmentsDirectly(it) }
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Randevu iptal edilemedi: ${result.message}"
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "İptal işlemi sırasında hata: ${e.message}"
                )
            }
        }
    }

    private fun loadCurrentUserAndStartObservingAppointments() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                getCurrentUserUseCase().collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val user = result.data
                            if (user != null) {
                                currentUserId = user.id
                                physiotherapistCache.clear()
                                loadAppointmentsDirectly(user.id)
                                startObservingAppointments(user.id)
                            } else {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = "Kullanıcı bilgisi alınamadı"
                                )
                            }
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Kullanıcı bilgisi alınırken hata oluştu: ${e.message}"
                )
            }
        }
    }

    private fun startObservingAppointments(userId: String) {
        appointmentsObservationJob?.cancel()
        appointmentsObservationJob = viewModelScope.launch {
            try {
                loadAppointmentsDirectly(userId)
            } catch (e: Exception) {
                loadAppointmentsDirectly(userId)
            }
        }
    }

    private fun loadAppointmentsDirectly(userId: String) {
        viewModelScope.launch {
            try {
                getUserAppointmentsUseCase(userId).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            val appointments = result.data ?: emptyList()
                            processAppointments(appointments)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message ?: "Randevular yüklenirken hata oluştu"
                            )
                        }
                        is Resource.Loading -> {
                            _state.value = _state.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Randevular yüklenirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun loadAppointments() {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                _state.value = _state.value.copy(isLoading = true)
                physiotherapistCache.clear()
                loadAppointmentsDirectly(userId)
            } ?: run {
                loadCurrentUserAndStartObservingAppointments()
            }
        }
    }

    private suspend fun processAppointments(appointments: List<Appointment>) {
        val currentDate = Date()

        val pastAppointments = appointments.filter { appointment ->
            appointment.date.before(currentDate)
        }

        val upcomingAppointments = appointments.filter { appointment ->
            val isFutureOrToday = appointment.date.after(currentDate) ||
                    (appointment.date.time >= currentDate.time - 24 * 60 * 60 * 1000)
            val shouldShow = isFutureOrToday &&
                    (appointment.status != AppointmentStatus.CANCELLED ||
                            appointment.cancelledBy == "physiotherapist")
            shouldShow
        }

        val pastAppointmentsWithPhysiotherapists = mutableListOf<AppointmentWithPhysiotherapist>()
        val upcomingAppointmentsWithPhysiotherapists = mutableListOf<AppointmentWithPhysiotherapist>()

        for (appointment in pastAppointments) {
            val appointmentWithPhysiotherapist = processAppointment(appointment)
            pastAppointmentsWithPhysiotherapists.add(appointmentWithPhysiotherapist)
        }

        for (appointment in upcomingAppointments) {
            val appointmentWithPhysiotherapist = processAppointment(appointment)
            upcomingAppointmentsWithPhysiotherapists.add(appointmentWithPhysiotherapist)
        }

        pastAppointmentsWithPhysiotherapists.sortByDescending { it.appointment.date }
        upcomingAppointmentsWithPhysiotherapists.sortBy { it.appointment.date }

        _state.value = _state.value.copy(
            isLoading = false,
            pastAppointments = pastAppointmentsWithPhysiotherapists,
            upcomingAppointments = upcomingAppointmentsWithPhysiotherapists,
            error = null
        )
    }

    private suspend fun processAppointment(appointment: Appointment): AppointmentWithPhysiotherapist {
        return try {
            var physiotherapist = physiotherapistCache[appointment.physiotherapistId]

            if (physiotherapist == null) {
                try {
                    val physiotherapistResult = getPhysiotherapistByIdUseCase(appointment.physiotherapistId).first()
                    physiotherapist = when (physiotherapistResult) {
                        is Resource.Success -> {
                            val data = physiotherapistResult.data
                            physiotherapistCache[appointment.physiotherapistId] = data
                            data
                        }
                        else -> null
                    }
                } catch (e: Exception) {
                }

                if (physiotherapist == null) {
                    try {
                        val profileDoc = firestore.collection("physiotherapist_profiles")
                            .document(appointment.physiotherapistId)
                            .get()
                            .await()
                        if (profileDoc.exists()) {
                            physiotherapist = profileDoc.toObject(PhysiotherapistProfile::class.java)
                            physiotherapistCache[appointment.physiotherapistId] = physiotherapist
                        }
                    } catch (e: Exception) {
                    }
                }
            }

            AppointmentWithPhysiotherapist(
                appointment = appointment,
                physiotherapistName = if (physiotherapist != null &&
                    (physiotherapist.firstName.isNotEmpty() || physiotherapist.lastName.isNotEmpty()))
                    "${physiotherapist.firstName} ${physiotherapist.lastName}".trim()
                else
                    "Fizyoterapist #${appointment.physiotherapistId.takeLast(4)}",
                physiotherapistPhotoUrl = if (physiotherapist != null && physiotherapist.profilePhotoUrl.isNotEmpty())
                    physiotherapist.profilePhotoUrl
                else
                    ""
            )
        } catch (e: Exception) {
            AppointmentWithPhysiotherapist(
                appointment = appointment,
                physiotherapistName = "Fizyoterapist",
                physiotherapistPhotoUrl = ""
            )
        }
    }

    fun clearSuccessMessage() {
        _state.value = _state.value.copy(successMessage = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        appointmentsObservationJob?.cancel()
        periodicRefreshJob?.cancel()
    }
}