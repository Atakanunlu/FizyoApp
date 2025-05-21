package com.example.fizyoapp.presentation.social.comments

import com.example.fizyoapp.domain.model.social.Comment

data class CommentsState(
    val isLoading: Boolean = false,
    val comments: List<Comment> = emptyList(),
    val commentContent: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)