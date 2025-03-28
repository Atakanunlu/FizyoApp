package com.example.fizyoapp.presentation.user

sealed class UserEvent {
    data object SignOut : UserEvent()
}