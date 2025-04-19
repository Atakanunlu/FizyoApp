package com.example.fizyoapp.domain.usecase.exercisesexamplesscreen

import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris
import com.example.fizyoapp.data.repository.exercisesexamplesscreen.ExercisesExamplesRepository
import com.example.fizyoapp.domain.model.exercisesexample.ExerciseCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExerciseCategoriesUseCase @Inject constructor(private val repository:ExercisesExamplesRepository) {
    operator fun invoke():Flow<List<OrnekEgzersizlerGiris>>{
        return repository.getExerciseCategories()
    }
}