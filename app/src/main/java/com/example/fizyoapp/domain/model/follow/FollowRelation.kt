package com.example.fizyoapp.domain.model.follow

import java.util.Date

data class FollowRelation(
    val id: String = "",
    val followerId: String = "",
    val followedId: String = "",
    val followerRole: String = "",
    val followedRole: String = "",
    val timestamp: Date = Date()
)