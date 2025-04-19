package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_profile_screen

import java.util.Date

data class PhysiotherapistProfileState(
    val userId: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: Date? = null,
    val gender: String = "",
    val city: String = "",
    val district: String = "",
    val fullAddress: String = "",
    val phoneNumber: String = "",
    val certificates: List<String> = emptyList(),
    val priceInfo: String = "Görüşme sonunda bilgilendirilecektir",
    val profilePhotoUrl: String = "",
    val tempPhotoUri: String = "",

    val firstNameError: Boolean = false,
    val lastNameError: Boolean = false,
    val birthDateError: Boolean = false,
    val genderError: Boolean = false,
    val cityError: Boolean = false,
    val districtError: Boolean = false,
    val fullAddressError: Boolean = false,
    val phoneNumberError: Boolean = false,
    val priceInfoError: Boolean = false,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isProfileSaved: Boolean = false
)