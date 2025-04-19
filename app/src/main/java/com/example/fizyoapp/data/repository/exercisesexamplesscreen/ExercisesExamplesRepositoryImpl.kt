// com.example.fizyoapp.data.repository.ExercisesExamplesRepositoryImpl.kt
package com.example.fizyoapp.data.repository

import com.example.fizyoapp.R
import com.example.fizyoapp.data.local.dao.exerciseexamplesscreen.OrnekEgzersizlerGirisDao
import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris
import com.example.fizyoapp.data.repository.exercisesexamplesscreen.ExercisesExamplesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExercisesExamplesRepositoryImpl @Inject constructor(
    private val exerciseCategoryDao: OrnekEgzersizlerGirisDao
) : ExercisesExamplesRepository {

    override fun getExerciseCategories(): Flow<List<OrnekEgzersizlerGiris>> {
        return exerciseCategoryDao.getAllCategories()
    }

    override suspend fun populateIfNeeded() {
        val count = exerciseCategoryDao.getCategoryCount()
        if (count == 0) {
            val categories = listOf(
                OrnekEgzersizlerGiris("shoulder", "Omuz Egzersizleri", R.drawable.omuz),
                OrnekEgzersizlerGiris("neck", "Boyun Egzersizleri", R.drawable.neck),
                OrnekEgzersizlerGiris("lower_back", "Bel Egzersizleri", R.drawable.bel),
                OrnekEgzersizlerGiris("leg", "Bacak Egzersizleri", R.drawable.bacak),
                OrnekEgzersizlerGiris("core", "Core Egzersizleri", R.drawable.core),
                OrnekEgzersizlerGiris("hip", "Kalça Egzersizleri", R.drawable.hip)
            )
            exerciseCategoryDao.insertAll(categories)
        }
    }
}