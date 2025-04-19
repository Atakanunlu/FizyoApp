package com.example.fizyoapp.presentation.bottomnavbar.items.searchscreen

import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile

data class SearchScreenState(
    val physiotherapists: List<PhysiotherapistProfile> = emptyList(),
    val filteredPhysiotherapists: List<PhysiotherapistProfile> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)