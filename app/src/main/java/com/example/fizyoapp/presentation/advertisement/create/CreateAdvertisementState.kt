package com.example.fizyoapp.presentation.advertisement.create

data class CreateAdvertisementState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasActiveAdvertisement: Boolean = false,
    val navigateToPayment: Boolean = false,
    val physiotherapistId: String = ""
)