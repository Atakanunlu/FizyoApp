package com.example.fizyoapp.presentation.socialmedia.socialmediamain

import com.example.fizyoapp.domain.model.socialmedia.Post

data class SocialMediaState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)