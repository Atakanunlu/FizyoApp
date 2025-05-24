package com.example.fizyoapp.presentation.advertisement.banner

import com.example.fizyoapp.domain.model.advertisement.Advertisement

data class AdvertisementBannerState(
    val isLoading: Boolean = true,
    val advertisements: List<Advertisement> = emptyList(),
    val currentIndex: Int = 0,
    val error: String? = null
) {
    val currentAdvertisement: Advertisement?
        get() = advertisements.getOrNull(currentIndex)
}