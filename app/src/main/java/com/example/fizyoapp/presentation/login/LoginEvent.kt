package com.example.fizyoapp.presentation.login

import com.example.fizyoapp.domain.model.auth.UserRole

sealed class LoginEvent {
    data class EmailChanged(val email: String): LoginEvent()
    data class PasswordChanged(val password: String): LoginEvent()
    data class RoleChanged(val role: UserRole): LoginEvent()
    data object SignIn: LoginEvent()
    data object NavigateToRegister: LoginEvent()
    data object NavigateToForgotPassword: LoginEvent()
    data object ResetState: LoginEvent()
}