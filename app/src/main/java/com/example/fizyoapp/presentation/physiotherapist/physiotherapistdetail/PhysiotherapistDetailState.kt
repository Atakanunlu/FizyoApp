package com.example.fizyoapp.presentation.physiotherapist.physiotherapistdetail

import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile

data class PhysiotherapistDetailState(
    val physiotherapist: PhysiotherapistProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)