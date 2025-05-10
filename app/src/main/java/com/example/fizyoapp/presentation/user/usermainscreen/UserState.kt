package com.example.fizyoapp.presentation.user.usermainscreen

import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord


data class UserState(
    val userName: String? = null,
    val userProfile: UserProfile? = null,
    val latestPainRecord: PainRecord? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val email: String = ""
)