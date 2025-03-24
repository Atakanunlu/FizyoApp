package com.example.fizyoapp.presentation.login

import com.example.fizyoapp.domain.model.UserRole

sealed class LoginEvent {
    //Kullanıcı bu inputlara veya butonabastığında tetiklenicek
    data class EmailChanged(val email: String): LoginEvent()
    data class PasswordChanged(val password: String): LoginEvent()
    data class RoleChanged(val role: UserRole): LoginEvent()

    data object SignIn: LoginEvent()
    data object NavigateToRegister: LoginEvent()
    data object ResetState: LoginEvent()
}