package com.example.fizyoapp.presentation.appointment.calendar

import com.example.fizyoapp.domain.model.appointment.Appointment
import com.example.fizyoapp.domain.model.appointment.BlockedTimeSlot
import java.util.Date

data class PhysiotherapistCalendarState(
    val appointments: List<Appointment> = emptyList(),
    val blockedTimeSlots: List<BlockedTimeSlot> = emptyList(),
    val availableTimeSlots: List<String> = emptyList(),
    val selectedDate: Date? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)