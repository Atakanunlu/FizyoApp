package com.example.fizyoapp.presentation.register

import com.example.fizyoapp.domain.model.auth.UserRole

sealed class RegisterEvent {

    data class EmailChanged(val email: String): RegisterEvent()
    data class PasswordChanged(val password: String): RegisterEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String): RegisterEvent()
    data class RoleChanged(val role: UserRole): RegisterEvent()

    data object SignUp: RegisterEvent()
    data object NavigateToLogin: RegisterEvent()
    data object ResetState: RegisterEvent()

}