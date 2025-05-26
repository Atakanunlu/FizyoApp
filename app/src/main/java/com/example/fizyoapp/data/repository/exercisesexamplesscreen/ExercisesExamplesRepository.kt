package com.example.fizyoapp.data.repository.exercisesexamplesscreen

import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris
import kotlinx.coroutines.flow.Flow

interface ExercisesExamplesRepository {
    fun getExerciseCategories(): Flow<List<OrnekEgzersizlerGiris>>
    suspend fun populateIfNeeded()
}