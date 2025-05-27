package com.example.fizyoapp.presentation.appointment.booking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.appointment.Appointment
import com.example.fizyoapp.domain.model.appointment.AppointmentStatus
import com.example.fizyoapp.domain.usecase.appointment.CreateAppointmentUseCase
import com.example.fizyoapp.domain.usecase.appointment.GetAvailableTimeSlotsUseCase
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AppointmentBookingViewModel @Inject constructor(
    private val getPhysiotherapistByIdUseCase: GetPhysiotherapistByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val createAppointmentUseCase: CreateAppointmentUseCase,
    private val getAvailableTimeSlotsUseCase: GetAvailableTimeSlotsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _state = MutableStateFlow(AppointmentBookingState())
    val state: StateFlow<AppointmentBookingState> = _state.asStateFlow()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var currentUserId: String? = null
    private var userName: String = ""
    private var userPhotoUrl: String = ""

    init {
        val physiotherapistId = savedStateHandle.get<String>("physiotherapistId") ?: ""
        if (physiotherapistId.isNotEmpty()) {
            _state.value = _state.value.copy(physiotherapistId = physiotherapistId)
            loadInitialData(physiotherapistId)
        } else {
            _state.value = _state.value.copy(
                error = "Fizyoterapist bilgisi bulunamadı",
                isLoading = false
            )
        }
    }

    private fun loadInitialData(physiotherapistId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                getPhysiotherapistByIdUseCase(physiotherapistId)
                    .catch { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Fizyoterapist bilgileri alınamadı: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    physiotherapist = result.data,
                                    isLoading = false
                                )
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = "Fizyoterapist bilgileri alınamadı: ${result.message}",
                                    isLoading = false
                                )
                            }
                            is Resource.Loading -> {
                            }
                        }
                    }

                getCurrentUserUseCase()
                    .catch { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Kullanıcı bilgileri alınamadı: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                val user = result.data
                                if (user != null) {
                                    currentUserId = user.id
                                    loadUserProfile(user.id)
                                } else {
                                    _state.value = _state.value.copy(
                                        error = "Kullanıcı bilgileri alınamadı",
                                        isLoading = false
                                    )
                                }
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = "Kullanıcı bilgileri alınamadı: ${result.message}",
                                    isLoading = false
                                )
                            }
                            is Resource.Loading -> {
                            }
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Veri yüklenirken hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                getUserProfileUseCase(userId)
                    .catch { e -> }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                val profile = result.data
                                if (profile != null) {
                                    userName = "${profile.firstName} ${profile.lastName}".trim()
                                    userPhotoUrl = profile.profilePhotoUrl
                                }
                            }
                            else -> { }
                        }
                    }
            } catch (e: Exception) { }
        }
    }

    private fun loadAvailableTimeSlots(date: Date) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                val physiotherapistId = _state.value.physiotherapistId
                if (physiotherapistId.isEmpty()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Fizyoterapist bilgisi bulunamadı"
                    )
                    return@launch
                }

                getAvailableTimeSlotsUseCase(physiotherapistId, date)
                    .catch { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Müsait saatler alınamadı: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    availableTimeSlots = result.data,
                                    isLoading = false
                                )
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = "Müsait saatler alınamadı: ${result.message}"
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
                    error = "Müsait saatler alınırken hata: ${e.message}"
                )
            }
        }
    }

    fun onEvent(event: AppointmentBookingEvent) {
        when (event) {
            is AppointmentBookingEvent.DateSelected -> {
                _state.value = _state.value.copy(
                    selectedDate = event.date,
                    selectedTimeSlot = null
                )
                loadAvailableTimeSlots(event.date)
            }
            is AppointmentBookingEvent.TimeSlotSelected -> {
                _state.value = _state.value.copy(selectedTimeSlot = event.timeSlot)
            }
            is AppointmentBookingEvent.AppointmentTypeSelected -> {
                _state.value = _state.value.copy(selectedAppointmentType = event.type)
            }
            is AppointmentBookingEvent.BookAppointment -> {
                bookAppointment()
            }
        }
    }

    private fun bookAppointment() {
        viewModelScope.launch {
            val currentState = _state.value
            val selectedDate = currentState.selectedDate
            val selectedTimeSlot = currentState.selectedTimeSlot
            val physiotherapistId = currentState.physiotherapistId
            val appointmentType = currentState.selectedAppointmentType

            if (currentUserId == null) {
                _state.value = _state.value.copy(error = "Kullanıcı bilgileri bulunamadı")
                return@launch
            }

            if (selectedDate == null) {
                _state.value = _state.value.copy(error = "Lütfen bir tarih seçin")
                return@launch
            }

            if (selectedTimeSlot == null) {
                _state.value = _state.value.copy(error = "Lütfen bir saat seçin")
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true)

            val appointment = Appointment(
                userId = currentUserId!!,
                physiotherapistId = physiotherapistId,
                date = selectedDate,
                timeSlot = selectedTimeSlot,
                status = AppointmentStatus.PENDING,
                userName = userName,
                userPhotoUrl = userPhotoUrl,
                appointmentType = appointmentType,
                rehabilitationNotes = ""
            )

            try {
                createAppointmentUseCase(appointment)
                    .catch { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Randevu oluşturulurken hata: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    isSuccess = true,
                                    error = null
                                )
                                _uiEvent.send(UiEvent.AppointmentBookedWithId(result.data.id))
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = "Randevu oluşturulamadı: ${result.message}"
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
                    error = "Randevu oluşturulurken hata: ${e.message}"
                )
            }
        }
    }

    sealed class UiEvent {
        data object AppointmentBooked : UiEvent()
        data class AppointmentBookedWithId(val appointmentId: String) : UiEvent()
    }
}