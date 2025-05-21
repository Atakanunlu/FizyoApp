package com.example.fizyoapp.presentation.social.create

data class CreatePostState(
    val content: String = "",
    val mediaUris: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isPostPublished: Boolean = false
)