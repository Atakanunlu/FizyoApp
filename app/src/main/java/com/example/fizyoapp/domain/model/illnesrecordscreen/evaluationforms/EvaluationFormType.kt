package com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms


import java.util.Date

enum class EvaluationFormType {
    PAIN_ASSESSMENT,
    FUNCTIONAL_MOBILITY,
    ROM_ASSESSMENT,
    MUSCLE_STRENGTH,
    POSTURE_ASSESSMENT,
    BALANCE_ASSESSMENT,
    CUSTOM
}


data class EvaluationForm(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val type: EvaluationFormType = EvaluationFormType.CUSTOM,
    val dateCreated: Date = Date(),
    val lastUpdated: Date = Date(),
    val isCompleted: Boolean = false,
    val questions: List<FormQuestion> = emptyList(),
    val answers: Map<String, String> = emptyMap(),
    val score: Int = 0,
    val maxScore: Int = 0
)

enum class QuestionType {
    TEXT,
    NUMBER,
    SCALE,
    MULTIPLE_CHOICE,
    YES_NO
}


data class FormQuestion(
    val id: String = "",
    val text: String = "",
    val type: QuestionType = QuestionType.TEXT,
    val options: List<String> = emptyList(),
    val required: Boolean = true,
    val minValue: Int? = null,
    val maxValue: Int? = null
)


data class FormResponse(
    val id: String = "",
    val formId: String = "",
    val userId: String = "",
    val answers: Map<String, String> = emptyMap(),
    val dateCompleted: Date = Date(),
    val score: Int = 0,
    val maxScore: Int = 0,
    val notes: String = "",
    val title: String = ""

)