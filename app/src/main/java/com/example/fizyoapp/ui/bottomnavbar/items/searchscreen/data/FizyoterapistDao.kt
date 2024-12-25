package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FizyoterapistDao {
    @Query("SELECT * FROM fizyolist WHERE name LIKE :name || '%'")
    suspend fun searchByName(name: String): Flow<List<FizyoterapistlerEntity>>

    @Query("SELECT * FROM fizyolist")
    suspend fun getAll():List<FizyoterapistlerEntity>

}