package com.example.fizyoapp.presentation.social.profile

import com.example.fizyoapp.domain.model.social.Post
import com.example.fizyoapp.domain.model.social.SocialProfile

data class SocialProfileState(
    val isLoading: Boolean = false,
    val profile: SocialProfile? = null,
    val posts: List<Post> = emptyList(),
    val errorMessage: String? = null,
    val isCurrentUser: Boolean = false
)