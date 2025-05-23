package com.example.fizyoapp.domain.model.appointment

import java.util.Date

data class BlockedTimeSlot(
    val id: String = "",
    val physiotherapistId: String = "",
    val date: Date = Date(),
    val timeSlot: String = "",
    val reason: String = ""
)