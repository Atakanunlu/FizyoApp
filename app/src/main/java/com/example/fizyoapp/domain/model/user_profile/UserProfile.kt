package com.example.fizyoapp.domain.model.user_profile

import java.util.Date

data class UserProfile(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: Date? = null,
    val gender: String = "",
    val city: String = "",
    val district: String = "",
    val phoneNumber: String = "",
    val profilePhotoUrl: String = "",
    val isProfileCompleted: Boolean = false
)