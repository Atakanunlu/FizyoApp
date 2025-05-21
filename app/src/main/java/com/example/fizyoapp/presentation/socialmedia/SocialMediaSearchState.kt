// presentation/socialmedia/SocialMediaSearchState.kt
package com.example.fizyoapp.presentation.socialmedia

import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile

data class SocialMediaSearchState(
    val searchQuery: String = "",
    val searchResults: List<PhysiotherapistProfile> = emptyList(),
    val searchHistory: List<PhysiotherapistProfile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasSearched: Boolean = false
)