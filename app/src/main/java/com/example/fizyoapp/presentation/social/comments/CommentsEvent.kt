package com.example.fizyoapp.presentation.social.comments

sealed class CommentsEvent {
    data object LoadComments : CommentsEvent()
    data class CommentContentChanged(val content: String) : CommentsEvent()
    data object SubmitComment : CommentsEvent()
}