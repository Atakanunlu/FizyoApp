package com.example.fizyoapp.domain.model.socialmedia

import java.util.Date

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val content: String = "",
    val mediaUrls: List<String> = emptyList(),
    val mediaTypes: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val timestamp: Date = Date(),
    val userRole: String = "",
    val likedBy: List<String> = emptyList()
)