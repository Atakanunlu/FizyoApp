package com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen

sealed class MessageDetailScreenEvent {
    data class MessageTextChanged(val text: String) : MessageDetailScreenEvent()
    object SendMessage : MessageDetailScreenEvent()
    object RefreshMessages : MessageDetailScreenEvent()
    object StartVideoCall : MessageDetailScreenEvent()
    data class EndVideoCall(
        val wasAnswered: Boolean = false,
        val metadata: Map<String, Any> = emptyMap()
    ) : MessageDetailScreenEvent()
    object DismissError : MessageDetailScreenEvent()

}