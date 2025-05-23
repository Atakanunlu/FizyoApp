package com.example.fizyoapp.presentation.forgotpassword

sealed class ResetPasswordEvent {
    data class PasswordChanged(val password: String) : ResetPasswordEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : ResetPasswordEvent()
    data object ResetPassword : ResetPasswordEvent()
    data object NavigateBack : ResetPasswordEvent()
}
