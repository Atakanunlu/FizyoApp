package com.example.fizyoapp.presentation.forgotpassword

sealed class ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent()
    data object SendResetEmail : ForgotPasswordEvent()
    data object NavigateBack : ForgotPasswordEvent()
}