package com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms

import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.EvaluationForm
import com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms.FormResponse
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread

data class EvaluationFormsState(
    val isLoading: Boolean = false,
    val forms: List<EvaluationForm> = emptyList(),
    val userResponses: List<FormResponse> = emptyList(),
    val error: String? = null,
    val actionError: String? = null,
    val successMessage: String? = null,
    val currentUserId: String = "",
    val recentThreads: List<ChatThread> = emptyList()

)