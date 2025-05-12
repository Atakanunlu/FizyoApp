package com.example.fizyoapp.presentation.user.usermainscreen

sealed class UserEvent {
    data object SignOut : UserEvent()
}