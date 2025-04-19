package com.example.fizyoapp.domain.usecase.exercisesexamplesscreen
import com.example.fizyoapp.data.repository.exercisesexamplesscreen.ExercisesExamplesRepository
import javax.inject.Inject

class PopulateDatabaseUseCase @Inject constructor(
    private val repository: ExercisesExamplesRepository
) {
    suspend operator fun invoke() {
        repository.populateIfNeeded()
    }
}