package com.example.fizyoapp.data.local.dao.exerciseexamplesscreen

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris
import com.example.fizyoapp.data.local.entity.exercisevideos.ExamplesOfExercisesEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface OrnekEgzersizlerGirisDao {
    @Query("SELECT * FROM exercise_categories")
    fun getAllCategories(): Flow<List<OrnekEgzersizlerGiris>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<OrnekEgzersizlerGiris>)

    @Query("SELECT COUNT(*) FROM exercise_categories")
    suspend fun getCategoryCount(): Int

}