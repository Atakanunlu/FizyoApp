package com.example.fizyoapp.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USER
)