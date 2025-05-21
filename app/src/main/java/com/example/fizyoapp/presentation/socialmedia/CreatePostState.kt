// presentation/socialmedia/CreatePostState.kt
package com.example.fizyoapp.presentation.socialmedia

data class CreatePostState(
    val content: String = "",
    val mediaUris: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String = "",
    val currentUserName: String = "",
    val currentUserPhotoUrl: String = "",
    val currentUserRole: String = ""
)