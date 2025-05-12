package com.example.fizyoapp.presentation.user.userprofile
import java.util.Date

data class UserProfileState(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: Date? = null,
    val gender: String = "",
    val city: String = "",
    val district: String = "",
    val phoneNumber: String = "",
    val profilePhotoUrl: String = "",

    val firstNameError: Boolean = false,
    val lastNameError: Boolean = false,
    val birthDateError: Boolean = false,
    val genderError: Boolean = false,
    val cityError: Boolean = false,
    val districtError: Boolean = false,
    val phoneNumberError: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isProfileSaved: Boolean = false
)