// presentation/socialmedia/PhysiotherapistSocialProfileState.kt
package com.example.fizyoapp.presentation.socialmedia

import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.model.socialmedia.Post

data class PhysiotherapistSocialProfileState(
    val profile: PhysiotherapistProfile? = null,
    val posts: List<Post> = emptyList(),
    val totalLikes: Int = 0,
    val totalComments: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)