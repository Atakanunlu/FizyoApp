package com.example.fizyoapp.presentation.advertisement.detail

import com.example.fizyoapp.domain.model.advertisement.Advertisement


data class AdvertisementDetailState(
    val isLoading: Boolean = true,
    val advertisement: Advertisement? = null,
    val physiotherapistName: String? = null,
    val physiotherapistImageUrl: String? = null,
    val error: String? = null
)