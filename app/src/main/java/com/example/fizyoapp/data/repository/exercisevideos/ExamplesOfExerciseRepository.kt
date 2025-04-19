package com.example.fizyoapp.data.repository.exercisevideos


import com.example.fizyoapp.data.local.entity.exercisevideos.ExamplesOfExercisesEntity
import kotlinx.coroutines.flow.Flow


interface ExamplesOfExerciseRepository {
    suspend fun insertVideos(videos: List<ExamplesOfExercisesEntity>)
    fun getVideosByCategory(category: String): Flow<List<ExamplesOfExercisesEntity>>
    suspend fun deleteVideosByCategory(category: String)
}