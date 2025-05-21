package com.example.fizyoapp.presentation.user.usermainscreen

sealed class UserEvent {
    object SignOut : UserEvent()
    object DismissError : UserEvent()
    object LoadUserProfile : UserEvent()
}