package com.example.fizyoapp.domain.model.auth

data class User(
    val id: String = "",
    val email: String = "",
    val role: UserRole = UserRole.USER
)