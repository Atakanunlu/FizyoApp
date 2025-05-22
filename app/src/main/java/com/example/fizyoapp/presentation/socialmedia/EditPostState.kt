package com.example.fizyoapp.presentation.socialmedia

data class EditPostState(
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val existingMediaUrls: List<String> = emptyList(),
    val existingMediaTypes: List<String> = emptyList(),
    val newMediaUris: List<String> = emptyList(),
    val userName: String = "",
    val userPhotoUrl: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdateSuccessful: Boolean = false
)