package com.example.fizyoapp.domain.model.illnesrecordscreen.evaluationforms


import java.util.Date

// Form tipleri için enum
enum class EvaluationFormType {
    PAIN_ASSESSMENT,       // Ağrı değerlendirmesi
    FUNCTIONAL_MOBILITY,   // Fonksiyonel mobilite
    ROM_ASSESSMENT,        // Eklem hareket açıklığı
    MUSCLE_STRENGTH,       // Kas gücü
    POSTURE_ASSESSMENT,    // Postür değerlendirmesi
    BALANCE_ASSESSMENT,
    CUSTOM
}

// Temel form sınıfı
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

// Soru tipleri için enum
enum class QuestionType {
    TEXT,           // Metin girişi
    NUMBER,         // Sayısal değer
    SCALE,          // Ölçek (1-10 gibi)
    MULTIPLE_CHOICE, // Çoktan seçmeli
    YES_NO        // Evet/Hayır// Vücut bölgesi seçimi
}

// Soru modeli
data class FormQuestion(
    val id: String = "",
    val text: String = "",
    val type: QuestionType = QuestionType.TEXT,
    val options: List<String> = emptyList(),
    val required: Boolean = true,
    val minValue: Int? = null,
    val maxValue: Int? = null
)

// Form yanıtları
data class FormResponse(
    val id: String = "",
    val formId: String = "",
    val userId: String = "",
    val answers: Map<String, String> = emptyMap(),
    val dateCompleted: Date = Date(),
    val score: Int = 0,
    val maxScore: Int = 0,
    val notes: String = "",
    val title: String = "" // Burayı ekledik

)