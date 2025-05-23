package com.example.fizyoapp.presentation.appointment.calendar

import java.util.Date

sealed class PhysiotherapistCalendarEvent {
    data class DateSelected(val date: Date) : PhysiotherapistCalendarEvent()
    data class BlockTimeSlot(val timeSlot: String, val reason: String) : PhysiotherapistCalendarEvent()
    data class UnblockTimeSlot(val blockedTimeSlotId: String) : PhysiotherapistCalendarEvent()
    data object Refresh : PhysiotherapistCalendarEvent()
}