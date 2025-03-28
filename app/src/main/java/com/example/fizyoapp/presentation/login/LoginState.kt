package com.example.fizyoapp.presentation.login

import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.auth.UserRole

data class LoginState(
    val name:String="",
    val surname:String="",
    val email: String = "",
    val password: String= "",
    val selectedRole: UserRole = UserRole.USER,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val user: User? = null
)
