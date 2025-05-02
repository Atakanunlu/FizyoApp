package com.example.fizyoapp.domain.model.usermainscreen


data class WaterIntake(
    val id: String = "",
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val glasses: Int = 0, // Bardak sayısı
    val milliliters: Int = 0 // Toplam ml
)