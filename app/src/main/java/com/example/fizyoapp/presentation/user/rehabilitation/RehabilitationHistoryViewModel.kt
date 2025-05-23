package com.example.fizyoapp.presentation.user.rehabilitation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.appointment.Appointment
import com.example.fizyoapp.domain.usecase.appointment.GetUserAppointmentsUseCase
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppointmentWithPhysiotherapist(
    val appointment: Appointment,
    val physiotherapistName: String,
    val physiotherapistPhotoUrl: String
)

@HiltViewModel
class RehabilitationHistoryViewModel @Inject constructor(
    private val getUserAppointmentsUseCase: GetUserAppointmentsUseCase,
    private val getPhysiotherapistByIdUseCase: GetPhysiotherapistByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(RehabilitationHistoryState())
    val state: StateFlow<RehabilitationHistoryState> = _state.asStateFlow()

    init {
        loadCurrentUserAndAppointments()
    }

    private fun loadCurrentUserAndAppointments() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getCurrentUserUseCase().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            loadAppointmentsForUser(user.id)
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
                    }
                }
            }
        }
    }

    fun loadAppointments() {
        loadCurrentUserAndAppointments()
    }

    private fun loadAppointmentsForUser(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getUserAppointmentsUseCase(userId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val appointments = result.data
                        val appointmentsWithPhysiotherapists = mutableListOf<AppointmentWithPhysiotherapist>()
                        appointments.forEach { appointment ->
                            try {
                                val physiotherapistResult = getPhysiotherapistByIdUseCase(appointment.physiotherapistId).first()
                                if (physiotherapistResult is Resource.Success) {
                                    val physiotherapist = physiotherapistResult.data
                                    appointmentsWithPhysiotherapists.add(
                                        AppointmentWithPhysiotherapist(
                                            appointment = appointment,
                                            physiotherapistName = "${physiotherapist.firstName} ${physiotherapist.lastName}",
                                            physiotherapistPhotoUrl = physiotherapist.profilePhotoUrl
                                        )
                                    )
                                } else {
                                    appointmentsWithPhysiotherapists.add(
                                        AppointmentWithPhysiotherapist(
                                            appointment = appointment,
                                            physiotherapistName = "Bilinmeyen Fizyoterapist",
                                            physiotherapistPhotoUrl = ""
                                        )
                                    )
                                }
                            } catch (e: Exception) {
                                appointmentsWithPhysiotherapists.add(
                                    AppointmentWithPhysiotherapist(
                                        appointment = appointment,
                                        physiotherapistName = "Bilinmeyen Fizyoterapist",
                                        physiotherapistPhotoUrl = ""
                                    )
                                )
                            }
                        }
                        _state.value = _state.value.copy(
                            isLoading = false,
                            appointments = appointmentsWithPhysiotherapists,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }
    }
}

data class RehabilitationHistoryState(
    val appointments: List<AppointmentWithPhysiotherapist> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)