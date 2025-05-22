package com.example.fizyoapp.presentation.socialmedia

sealed class EditPostEvent {
    data class ContentChanged(val content: String) : EditPostEvent()
    data class MediaAdded(val uris: List<String>) : EditPostEvent()
    data class NewMediaRemoved(val uri: String) : EditPostEvent()
    data class ExistingMediaRemoved(val uri: String) : EditPostEvent()
    data object UpdatePost : EditPostEvent()
}