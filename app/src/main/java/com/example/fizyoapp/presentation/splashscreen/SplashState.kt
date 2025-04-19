package com.example.fizyoapp.presentation.splashscreen

import com.example.fizyoapp.domain.model.auth.UserRole

data class SplashState(
    val isUserLoggedIn: Boolean = false,
    val isProfileCompleted: Boolean = false,
    val userRole: UserRole? = null,
    val isLoading: Boolean = true
)