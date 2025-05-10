package com.example.fizyoapp.presentation.user.usermainscreen

sealed class UserEvent {
    object Logout : UserEvent()
    object DismissError : UserEvent()
}