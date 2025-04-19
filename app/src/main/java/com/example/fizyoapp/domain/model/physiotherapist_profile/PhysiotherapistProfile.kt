package com.example.fizyoapp.domain.model.physiotherapist_profile

import java.util.Date

data class PhysiotherapistProfile(
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
    val profilePhotoData: Map<String, Any>? = null,
    val isProfileCompleted: Boolean = false
)