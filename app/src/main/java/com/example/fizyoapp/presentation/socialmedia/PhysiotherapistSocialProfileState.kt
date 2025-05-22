package com.example.fizyoapp.presentation.socialmedia

import com.example.fizyoapp.domain.model.follow.FollowRelation
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.model.socialmedia.Post


data class PhysiotherapistSocialProfileState(
    val profile: PhysiotherapistProfile? = null,
    val posts: List<Post> = emptyList(),
    val totalLikes: Int = 0,
    val totalComments: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val followers: List<FollowRelation> = emptyList(),
    val following: List<FollowRelation> = emptyList(),
    val followerProfiles: Map<String, Any> = emptyMap(),
    val followingProfiles: Map<String, PhysiotherapistProfile> = emptyMap(),
    val isFollowing: Boolean = false,
    val isFollowLoading: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showFollowers: Boolean = false,
    val showFollowing: Boolean = false
)