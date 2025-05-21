package com.example.fizyoapp.presentation.social.profile

sealed class SocialProfileEvent {
    data object LoadProfile : SocialProfileEvent()
    data object LoadUserPosts : SocialProfileEvent()
    data object ToggleFollow : SocialProfileEvent()
    data class ToggleLike(val postId: String) : SocialProfileEvent()
    data class ShowComments(val postId: String) : SocialProfileEvent()
}