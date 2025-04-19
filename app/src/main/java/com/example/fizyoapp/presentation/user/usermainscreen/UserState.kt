package com.example.fizyoapp.presentation.user.usermainscreen

import com.example.fizyoapp.domain.model.auth.User

data class UserState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)