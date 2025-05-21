package com.example.fizyoapp.presentation.social.feed

import com.example.fizyoapp.domain.model.social.Post

data class SocialFeedState(
    val isLoading: Boolean = false,
    val generalPosts: List<Post> = emptyList(),
    val followingPosts: List<Post> = emptyList(),
    val showFollowingFeed: Boolean = false,
    val errorMessage: String? = null,
    val isPhysiotherapist: Boolean = false
)