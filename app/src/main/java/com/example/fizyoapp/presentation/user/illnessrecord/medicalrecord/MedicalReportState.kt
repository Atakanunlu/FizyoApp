package com.example.fizyoapp.presentation.user.illnessrecord.medicalrecord

import com.example.fizyoapp.domain.model.illnesrecordscreen.medicalrecord.MedicalReport
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread

data class MedicalReportState(
    val isLoading: Boolean = false,
    val reports: List<MedicalReport> = emptyList(),
    val error: String? = null,
    val actionError: String? = null,
    val successMessage: String? = null,
    val currentUserId: String = "",
    val recentThreads: List<ChatThread> = emptyList()
)
