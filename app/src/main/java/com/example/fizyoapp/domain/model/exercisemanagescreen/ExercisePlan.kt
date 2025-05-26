
package com.example.fizyoapp.domain.model.exercisemanagescreen

import java.util.Date

data class Exercise(
    val id: String = "",
    val physiotherapistId: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val mediaUrls: List<String> = emptyList(),
    val mediaType: Map<String, ExerciseType> = emptyMap(),
    val instructions: String = "",
    val duration: Int = 0,
    val repetitions: Int = 0,
    val sets: Int = 0,
    val difficulty: ExerciseDifficulty = ExerciseDifficulty.MEDIUM,
    val isTemplate: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)



enum class ExerciseDifficulty {
    EASY, MEDIUM, HARD
}

val DEFAULT_EXERCISE_CATEGORIES = listOf(
    "Omuz Egzersizleri",
    "Kalça Egzersizleri",
    "Diz Egzersizleri",
    "Boyun Egzersizleri",
    "Sırt Egzersizleri",
    "Bel Egzersizleri",
    "El-Bilek Egzersizleri",
    "Ayak-Ayak Bileği Egzersizleri",
    "Kardiyovasküler Egzersizler",
    "Kuvvet Egzersizleri",
    "Denge Egzersizleri",
    "Esneklik Egzersizleri",
    "Genel Fiziksel Aktiviteler"
)

data class ExercisePlan(
    val id: String = "",
    val physiotherapistId: String = "",
    val patientId: String = "",
    val title: String = "",
    val description: String = "",
    val exercises: List<ExercisePlanItem> = emptyList(),
    val startDate: Date? = null,
    val endDate: Date? = null,
    val frequency: String = "",
    val notes: String = "",
    val status: ExercisePlanStatus = ExercisePlanStatus.ACTIVE,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)

data class ExercisePlanItem(
    val exerciseId: String = "",
    val exerciseTitle: String = "",
    val sets: Int = 0,
    val repetitions: Int = 0,
    val duration: Int = 0,
    val notes: String = "",
    val mediaUrls: List<String> = emptyList(),
    val mediaTypes: Map<String, ExerciseType> = emptyMap() // Yeni eklenen alan
)

enum class ExercisePlanStatus {
    ACTIVE, COMPLETED, CANCELLED
}
enum class ExerciseType {
    VIDEO, IMAGE
}