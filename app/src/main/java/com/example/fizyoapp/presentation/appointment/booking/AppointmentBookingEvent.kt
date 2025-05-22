package com.example.fizyoapp.presentation.appointment.booking

import com.example.fizyoapp.domain.model.appointment.AppointmentType
import java.util.Date

sealed class AppointmentBookingEvent {
    data class DateSelected(val date: Date) : AppointmentBookingEvent()
    data class TimeSlotSelected(val timeSlot: String) : AppointmentBookingEvent()
    data class AppointmentTypeSelected(val type: AppointmentType) : AppointmentBookingEvent()
    data object BookAppointment : AppointmentBookingEvent()
}