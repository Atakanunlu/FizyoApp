// domain/model/socialmedia/Comment.kt
package com.example.fizyoapp.domain.model.socialmedia

import java.util.Date

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val content: String = "",
    val timestamp: Date = Date(),
    val userRole: String = ""
)