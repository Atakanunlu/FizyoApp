package com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen

sealed class MessagesScreenEvent {
    data class NavigateToMessageDetail(val userId: String) : MessagesScreenEvent()
    object RefreshChatThreads : MessagesScreenEvent()
    object DismissError : MessagesScreenEvent() // Yeni eklendi
}