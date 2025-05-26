package com.example.fizyoapp.data.repository.exercisemanagescreen

import android.net.Uri
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.exercisemanagescreen.Exercise
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlan
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getExercisesByPhysiotherapist(physiotherapistId: String): Flow<Resource<List<Exercise>>>
    fun getExerciseById(exerciseId: String): Flow<Resource<Exercise>>
    fun createExercise(exercise: Exercise): Flow<Resource<Exercise>>
    fun updateExercise(exercise: Exercise): Flow<Resource<Exercise>>
    fun deleteExercise(exerciseId: String): Flow<Resource<Boolean>>
    fun uploadExerciseMedia(
        mediaUri: Uri,
        physiotherapistId: String,
        fileName: String
    ): Flow<Resource<String>>
    fun getExercisePlansByPhysiotherapist(physiotherapistId: String): Flow<Resource<List<ExercisePlan>>>
    fun getExercisePlansByPatient(patientId: String): Flow<Resource<List<ExercisePlan>>>
    fun createExercisePlan(exercisePlan: ExercisePlan): Flow<Resource<ExercisePlan>>
    fun updateExercisePlan(exercisePlan: ExercisePlan): Flow<Resource<ExercisePlan>>
    fun deleteExercisePlan(exercisePlanId: String): Flow<Resource<Boolean>> // Boolean değil, Boolean olmalı
    fun getExercisePlanById(planId: String): Flow<Resource<ExercisePlan>> // Exercise değil, ExercisePlan olmalı
    fun getPatientsList(physiotherapistId: String): Flow<Resource<List<PatientListItem>>>
}

data class PatientListItem(
    val userId: String,
    val fullName: String,
    val profilePhotoUrl: String?
)