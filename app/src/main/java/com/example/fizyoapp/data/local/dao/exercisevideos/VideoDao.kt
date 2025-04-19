package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fizyoapp.data.local.entity.ExamplesOfExercisesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(videoEntity: ExamplesOfExercisesEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<ExamplesOfExercisesEntity>)

    @Query("SELECT * FROM videos")
    fun getAllVideos(): Flow<List<ExamplesOfExercisesEntity>>

    @Query("DELETE FROM videos")
    suspend fun deleteAllVideos()

    @Query("SELECT * FROM videos WHERE category = :category")
    fun getVideosByCategory(category: String): Flow<List<ExamplesOfExercisesEntity>>

    @Query("DELETE FROM videos WHERE category = :category")
    suspend fun deleteVideosByCategory(category: String)
}
