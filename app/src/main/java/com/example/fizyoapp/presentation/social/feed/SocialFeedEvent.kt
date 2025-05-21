package com.example.fizyoapp.presentation.social.feed

sealed class SocialFeedEvent {
    data object LoadGeneralPosts : SocialFeedEvent()
    data object LoadFollowingPosts : SocialFeedEvent()
    data class ToggleLike(val postId: String) : SocialFeedEvent()
    data class ShowComments(val postId: String) : SocialFeedEvent()
    data class ToggleFollow(val authorId: String) : SocialFeedEvent()
    data class NavigateToProfile(val userId: String) : SocialFeedEvent()
    data object NavigateToCreatePost : SocialFeedEvent()
}