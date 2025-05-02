package com.example.fizyoapp.domain.model.usermainscreen

data class PainRecord(
    val id: String = "",
    val userId: String = "",
    val intensity: Int = 0, // 1-10 arası ağrı şiddeti
    val location: String = "", // Ağrının yeri
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
)
