package com.example.fizyoapp.presentation.register

import com.example.fizyoapp.domain.model.UserRole

data class RegisterState (
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val selectedRole: UserRole = UserRole.USER,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationSuccessful: Boolean = false,
    val passwordError: Boolean = false
)