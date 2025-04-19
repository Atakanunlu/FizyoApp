package com.example.fizyoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.fizyoapp.data.local.entity.OrnekEgzersizlerGiris


@Dao
interface OrnekEgzersizlerGirisDao {
    @Query("SELECT * FROM ornekegzersizlergiris")
    fun getAll():List<OrnekEgzersizlerGiris>
}