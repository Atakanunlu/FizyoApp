package com.example.fizyoapp.presentation.user.ornekegzersizler


import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris


sealed class ExercisesExamplesEvent {

    data class CategorySelected(val category:OrnekEgzersizlerGiris):ExercisesExamplesEvent()
    object LoadCategories:ExercisesExamplesEvent()
    object CategoryNavigationHandled : ExercisesExamplesEvent()

}