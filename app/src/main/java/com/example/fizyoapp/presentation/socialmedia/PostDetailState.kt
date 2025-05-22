package com.example.fizyoapp.presentation.socialmedia

import com.example.fizyoapp.domain.model.socialmedia.Comment
import com.example.fizyoapp.domain.model.socialmedia.Post

data class PostDetailState(
    val post: Post? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isCommentLoading: Boolean = false,
    val error: String? = null,
    val currentUserName: String = "",
    val currentUserPhotoUrl: String = "",
    val currentUserId: String = "",
    val isPostLikedByCurrentUser: Boolean = false,
    val postDeleted: Boolean = false
)