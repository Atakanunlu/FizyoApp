package com.example.fizyoapp.domain.model.usermainscreen

data class PainRecord(
    val id: String = "",
    val userId: String = "",
    val intensity: Int = 0,
    val location: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)


