package com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen

import com.example.fizyoapp.domain.model.messagesscreen.ChatThread

data class MessagesScreenState(
    val chatThreads: List<ChatThread> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUserId: String = ""
)