package com.example.fizyoapp.domain.model.usermainscreen

data class Reminder(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val isActive: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)