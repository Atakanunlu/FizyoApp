package com.example.fizyoapp.presentation.social.create

sealed class CreatePostEvent {
    data class ContentChanged(val content: String) : CreatePostEvent()
    data class AddMedia(val uri: String) : CreatePostEvent()
    data class RemoveMedia(val index: Int) : CreatePostEvent()
    data object PublishPost : CreatePostEvent()
    data object CancelPost : CreatePostEvent()
}