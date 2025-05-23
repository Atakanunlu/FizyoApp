package com.example.fizyoapp.presentation.forgotpassword

data class ResetPasswordState(
    val email: String = "",
    val code: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPasswordReset: Boolean = false,
    val passwordError: Boolean = false
)