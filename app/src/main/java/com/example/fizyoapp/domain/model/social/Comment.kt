package com.example.fizyoapp.domain.model.social

import java.util.Date

data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val content: String = "",
    val createdAt: Date = Date()
)