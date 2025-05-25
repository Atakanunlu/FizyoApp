package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_main_screen

import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile

data class PhysiotherapistState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val physiotherapistId: String? = null,
    val physiotherapistName: String? = null,
    val physiotherapistProfile: PhysiotherapistProfile? = null
)