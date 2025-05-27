package com.example.fizyoapp.presentation.appointment.booking

import com.example.fizyoapp.domain.model.appointment.AppointmentType
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import java.util.Date

data class AppointmentBookingState(
    val physiotherapistId: String = "",
    val physiotherapist: PhysiotherapistProfile? = null,
    val selectedDate: Date? = null,
    val availableTimeSlots: List<String> = emptyList(),
    val selectedTimeSlot: String? = null,
    val selectedAppointmentType: AppointmentType = AppointmentType.IN_PERSON,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)