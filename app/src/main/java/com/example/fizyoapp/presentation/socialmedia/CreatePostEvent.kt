package com.example.fizyoapp.presentation.socialmedia

sealed class CreatePostEvent {
    data class ContentChanged(val content: String) : CreatePostEvent()
    data class MediaAdded(val uris: List<String>) : CreatePostEvent()
    data class MediaRemoved(val uri: String) : CreatePostEvent()
    data object CreatePost : CreatePostEvent()
}