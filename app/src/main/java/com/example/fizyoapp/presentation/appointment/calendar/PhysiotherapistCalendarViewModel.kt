package com.example.fizyoapp.presentation.appointment.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.appointment.BlockedTimeSlot
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.usecase.appointment.BlockTimeSlotUseCase
import com.example.fizyoapp.domain.usecase.appointment.GetAvailableTimeSlotsUseCase
import com.example.fizyoapp.domain.usecase.appointment.GetPhysiotherapistAppointmentsUseCase
import com.example.fizyoapp.domain.usecase.appointment.UnblockTimeSlotUseCase
import com.example.fizyoapp.domain.usecase.appointment.UpdateAppointmentNotesUseCase
import com.example.fizyoapp.domain.usecase.auth.GetCurrentPhysiotherapistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class PhysiotherapistCalendarViewModel @Inject constructor(
    private val getPhysiotherapistAppointmentsUseCase: GetPhysiotherapistAppointmentsUseCase,
    private val getAvailableTimeSlotsUseCase: GetAvailableTimeSlotsUseCase,
    private val blockTimeSlotUseCase: BlockTimeSlotUseCase,
    private val unblockTimeSlotUseCase: UnblockTimeSlotUseCase,
    private val updateAppointmentNotesUseCase: UpdateAppointmentNotesUseCase,
    private val getCurrentPhysiotherapistUseCase: GetCurrentPhysiotherapistUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(PhysiotherapistCalendarState())
    val state: StateFlow<PhysiotherapistCalendarState> = _state.asStateFlow()
    private var currentUserId: String? = null

    init {
        getCurrentPhysiotherapist()
        val today = Calendar.getInstance().time
        onEvent(PhysiotherapistCalendarEvent.DateSelected(today))
    }

    private fun getCurrentPhysiotherapist() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                getCurrentPhysiotherapistUseCase()
                    .catch { e ->
                        _state.value = _state.value.copy(
                            error = "Kullanıcı bilgileri alınamadı: ${e.message}",
                            isLoading = false
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                val user = result.data
                                if (user.role == UserRole.PHYSIOTHERAPIST) {
                                    currentUserId = user.id
                                    loadAppointments(user.id)
                                    val selectedDate = _state.value.selectedDate
                                    if (selectedDate != null) {
                                        loadAvailableTimeSlots(selectedDate)
                                    }
                                    _state.value = _state.value.copy(isLoading = false)
                                } else {
                                    _state.value = _state.value.copy(
                                        error = "Fizyoterapist hesabı bulunamadı",
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
                    error = "Kullanıcı bilgileri alınırken hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun onEvent(event: PhysiotherapistCalendarEvent) {
        when (event) {
            is PhysiotherapistCalendarEvent.DateSelected -> {
                _state.value = _state.value.copy(
                    selectedDate = event.date,
                    error = null,
                    success = null
                )
                if (currentUserId != null) {
                    loadAvailableTimeSlots(event.date)
                }
            }
            is PhysiotherapistCalendarEvent.BlockTimeSlot -> {
                blockTimeSlot(event.timeSlot, event.reason)
            }
            is PhysiotherapistCalendarEvent.UnblockTimeSlot -> {
                unblockTimeSlot(event.blockedTimeSlotId)
            }
            is PhysiotherapistCalendarEvent.Refresh -> {
                getCurrentPhysiotherapist()
                val selectedDate = _state.value.selectedDate ?: Calendar.getInstance().time
                if (currentUserId != null) {
                    loadAvailableTimeSlots(selectedDate)
                    loadAppointments(currentUserId!!)
                }
            }
        }
    }

    private fun loadAppointments(physiotherapistId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                getPhysiotherapistAppointmentsUseCase(physiotherapistId)
                    .catch { e ->
                        _state.value = _state.value.copy(
                            error = "Randevular alınamadı: ${e.message}",
                            isLoading = false
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    appointments = result.data,
                                    isLoading = false
                                )
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    error = "Randevular alınamadı: ${result.message}",
                                    isLoading = false
                                )
                            }
                            is Resource.Loading -> {
                                _state.value = _state.value.copy(isLoading = true)
                            }
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Randevular alınırken hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun loadAvailableTimeSlots(date: Date) {
        viewModelScope.launch {
            if (currentUserId == null) {
                _state.value = _state.value.copy(
                    error = "Kullanıcı bilgisi alınamadı",
                    isLoading = false
                )
                return@launch
            }
            try {
                _state.value = _state.value.copy(isLoading = true)
                getAvailableTimeSlotsUseCase(currentUserId!!, date)
                    .catch { e ->
                        _state.value = _state.value.copy(
                            error = "Müsait saatler alınamadı: ${e.message}",
                            isLoading = false
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
                                    error = "Müsait saatler alınamadı: ${result.message}",
                                    isLoading = false
                                )
                            }
                            is Resource.Loading -> {
                                _state.value = _state.value.copy(isLoading = true)
                            }
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Müsait saatler alınırken hata oluştu: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    private fun blockTimeSlot(timeSlot: String, reason: String) {
        viewModelScope.launch {
            if (currentUserId == null) {
                _state.value = _state.value.copy(
                    error = "Kullanıcı bilgisi alınamadı",
                    isLoading = false
                )
                return@launch
            }
            val selectedDate = _state.value.selectedDate
            if (selectedDate == null) {
                _state.value = _state.value.copy(
                    error = "Lütfen bir tarih seçin",
                    isLoading = false
                )
                return@launch
            }
            try {
                _state.value = _state.value.copy(isLoading = true)
                val blockedTimeSlot = BlockedTimeSlot(
                    physiotherapistId = currentUserId!!,
                    date = selectedDate,
                    timeSlot = timeSlot,
                    reason = reason
                )
                blockTimeSlotUseCase(blockedTimeSlot)
                    .catch { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Saat dilimi bloke edilemedi: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    success = "Saat dilimi başarıyla bloke edildi"
                                )
                                loadAvailableTimeSlots(selectedDate)
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = "Saat dilimi bloke edilemedi: ${result.message}"
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
                    error = "Saat dilimi bloke edilirken hata oluştu: ${e.message}"
                )
            }
        }
    }

    private fun unblockTimeSlot(blockedTimeSlotId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                unblockTimeSlotUseCase(blockedTimeSlotId)
                    .catch { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Saat dilimi blokajı kaldırılamadı: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    success = "Saat dilimi blokajı kaldırıldı"
                                )
                                val selectedDate = _state.value.selectedDate
                                if (selectedDate != null && currentUserId != null) {
                                    loadAvailableTimeSlots(selectedDate)
                                }
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = "Saat dilimi blokajı kaldırılamadı: ${result.message}"
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
                    error = "Saat dilimi blokajı kaldırılırken hata oluştu: ${e.message}"
                )
            }
        }
    }

    fun updateRehabilitationNotes(appointmentId: String, notes: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                updateAppointmentNotesUseCase(appointmentId, notes)
                    .catch { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Notlar güncellenirken hata oluştu: ${e.message}"
                        )
                    }
                    .collect { result ->
                        when (result) {
                            is Resource.Success -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    success = "Rehabilitasyon notları güncellendi",
                                    error = null
                                )
                                if (currentUserId != null) {
                                    loadAppointments(currentUserId!!)
                                }
                            }
                            is Resource.Error -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = "Notlar güncellenemedi: ${result.message}"
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
                    error = "Notlar güncellenirken hata oluştu: ${e.message}"
                )
            }
        }
    }
}