package com.example.fizyoapp.presentation.user.ornekegzersizler

import androidx.annotation.DrawableRes
import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris
import com.example.fizyoapp.domain.model.exercisesexample.ExerciseCategory

data class ExercisesExamplesState(
    val categories:List<OrnekEgzersizlerGiris> = emptyList(),
    val isLoading:Boolean=false,
    val error:String?=null,
    val selectedCategoryId: String? = null // Sadece seçilen kategori ID'sini tutuyoruz

)
