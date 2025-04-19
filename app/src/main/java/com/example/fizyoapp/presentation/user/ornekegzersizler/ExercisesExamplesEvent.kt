package com.example.fizyoapp.presentation.user.ornekegzersizler

import com.example.fizyoapp.data.local.dao.exerciseexamplesscreen.OrnekEgzersizlerGirisDao
import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris
import com.example.fizyoapp.domain.model.exercisesexample.ExerciseCategory

sealed class ExercisesExamplesEvent {

    data class CategorySelected(val category:OrnekEgzersizlerGiris):ExercisesExamplesEvent()
    object LoadCategories:ExercisesExamplesEvent()
    object CategoryNavigationHandled : ExercisesExamplesEvent() // Navigasyon tamamlandığını bildir

}