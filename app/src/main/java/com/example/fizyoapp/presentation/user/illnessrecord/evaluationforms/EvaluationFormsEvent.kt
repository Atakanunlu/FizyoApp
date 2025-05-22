package com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms

sealed class EvaluationFormsEvent {
    object RefreshData : EvaluationFormsEvent()
    object DismissError : EvaluationFormsEvent()
    data class ShareFormResponse(val responseId: String, val userId: String) : EvaluationFormsEvent()
    data class DeleteFormResponse(val responseId: String) : EvaluationFormsEvent()
}