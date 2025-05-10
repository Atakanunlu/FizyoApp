package com.example.fizyoapp.presentation.user.usermainscreen

import com.example.fizyoapp.domain.model.usermainscreen.PainRecord

data class PainTrackingState(
    val painRecords: List<PainRecord> = emptyList(),
    val currentPainRecord: PainRecord? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddingRecord: Boolean = false,
    val painIntensity: Int = 0,
    val painLocation: String = "",
    val painDescription: String = ""
)
