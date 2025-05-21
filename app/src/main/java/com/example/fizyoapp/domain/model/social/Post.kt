package com.example.fizyoapp.domain.model.social

import java.util.Date

data class Post(
    val id: String = "",
    val authorId: String = "",
    val content: String = "",
    val mediaUrls: List<String> = emptyList(),
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Date = Date(),
    val isLikedByCurrentUser: Boolean = false
)