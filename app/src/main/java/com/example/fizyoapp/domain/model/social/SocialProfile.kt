package com.example.fizyoapp.domain.model.social

data class SocialProfile(
    val userId: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isFollowedByCurrentUser: Boolean = false
)