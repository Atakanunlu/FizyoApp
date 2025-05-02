package com.example.fizyoapp.domain.model.usermainscreen

data class StepCount(
    val id: String = "",
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val steps: Int = 0
)
