package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.repository


import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.model.ExamplesOfExercisesEntity
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.model.VideoDao
import kotlinx.coroutines.flow.Flow


interface ExamplesOfExerciseRepository {
    suspend fun insertVideo(video: ExamplesOfExercisesEntity)
    suspend fun insertVideos(videos: List<ExamplesOfExercisesEntity>)
    fun getAllVideo(): Flow<List<ExamplesOfExercisesEntity>>
    suspend fun deleteAllVideos()
    suspend fun insertVideoWithCategory(video: ExamplesOfExercisesEntity, category: String)
    fun getVideosByCategory(category: String): Flow<List<ExamplesOfExercisesEntity>>
    suspend fun deleteVideosByCategory(category: String)
}