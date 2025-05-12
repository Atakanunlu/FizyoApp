package com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen

import com.example.fizyoapp.domain.model.messagesscreen.Message
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.model.user_profile.UserProfile

data class MessageDetailScreenState(
    val messages: List<Message> = emptyList(),
    val currentUserId: String = "",
    val messageText: String = "",
    val isSending: Boolean = false,
    val isPhysiotherapist: Boolean = false,
    val physiotherapist: PhysiotherapistProfile? = null,
    val user: UserProfile? = null,
    val isVideoCallActive: Boolean = false,
    val isInitialLoading: Boolean = true, // Yeni eklenen alan
    val isLoading: Boolean = false,
    val error: String? = null
)