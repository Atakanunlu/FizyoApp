package com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen

import com.example.fizyoapp.domain.model.messagesscreen.Message
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.model.user_profile.UserProfile

data class MessageDetailScreenState(
    val messages: List<Message> = emptyList(),
    val messageText: String = "",
    val physiotherapist: PhysiotherapistProfile? = null,
    val user: UserProfile? = null,
    val isPhysiotherapist: Boolean = false,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null,
    val currentUserId: String = "",
    val isVideoCallActive: Boolean = false

)