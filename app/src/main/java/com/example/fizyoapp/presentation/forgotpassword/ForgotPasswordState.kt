package com.example.fizyoapp.presentation.forgotpassword

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmailSent: Boolean = false
)