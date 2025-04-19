package com.example.fizyoapp.data.local.dao.exercisevideos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fizyoapp.data.local.entity.exercisevideos.ExamplesOfExercisesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<ExamplesOfExercisesEntity>)

    @Query("SELECT * FROM videos WHERE category = :category")
    fun getVideosByCategory(category: String): Flow<List<ExamplesOfExercisesEntity>>

    @Query("DELETE FROM videos WHERE category = :category")
    suspend fun deleteVideosByCategory(category: String)
}
