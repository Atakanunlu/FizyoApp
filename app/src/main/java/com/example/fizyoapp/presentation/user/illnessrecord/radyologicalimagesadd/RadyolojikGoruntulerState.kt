package com.example.fizyoapp.presentation.user.illnessrecord.radyologicalimagesadd

import com.example.fizyoapp.domain.model.messagesscreen.ChatThread

data class RadyolojikGoruntulerState(
    val goruntular: List<RadyolojikGoruntu> = emptyList(),
    val recentThreads: List<ChatThread> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val actionError: String? = null,
    val successMessage: String? = null,
    val currentUserId: String = ""
)