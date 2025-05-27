package com.example.fizyoapp.domain.model.appointment

import java.util.Date

data class Appointment(
    val id: String = "",
    val userId: String = "",
    val physiotherapistId: String = "",
    val date: Date = Date(),
    val timeSlot: String = "",
    val status: AppointmentStatus = AppointmentStatus.PENDING,
    val userName: String = "",
    val userPhotoUrl: String = "",
    val appointmentType: AppointmentType = AppointmentType.IN_PERSON,
    val rehabilitationNotes: String = "",
    val createdAt: Date = Date(),
    val cancelledBy: String = "",
    val cancelledAt: Date? = null
)

enum class AppointmentStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED
}

enum class AppointmentType {
    IN_PERSON,
    REMOTE
}