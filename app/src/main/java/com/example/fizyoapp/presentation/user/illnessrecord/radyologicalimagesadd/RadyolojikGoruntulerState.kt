package com.example.fizyoapp.presentation.user.illnessrecord.radyologicalimagesadd

import com.example.fizyoapp.domain.model.messagesscreen.ChatThread

data class RadyolojikGoruntulerState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val actionError: String? = null,
    val successMessage: String? = null,
    val goruntular: List<RadyolojikGoruntu> = emptyList(),
    val currentUserId: String = "",
    val recentThreads: List<ChatThread> = emptyList()
)