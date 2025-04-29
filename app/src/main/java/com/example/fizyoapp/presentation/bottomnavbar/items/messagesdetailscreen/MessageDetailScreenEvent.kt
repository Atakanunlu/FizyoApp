package com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen

sealed class MessageDetailScreenEvent {
    data class MessageTextChanged(val text: String) : MessageDetailScreenEvent()
    object SendMessage : MessageDetailScreenEvent()
    object RefreshMessages : MessageDetailScreenEvent()
}