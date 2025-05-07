package com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen

sealed class MessagesScreenUiEvent {
    data class NavigateToMessageDetail(val userId: String) : MessagesScreenUiEvent()
}