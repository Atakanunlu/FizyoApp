package com.example.fizyoapp.presentation.user.ornekegzersizler

import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris

data class ExercisesExamplesState(
    val categories:List<OrnekEgzersizlerGiris> = emptyList(),
    val isLoading:Boolean=false,
    val error:String?=null,
    val selectedCategoryId: String? = null

)
