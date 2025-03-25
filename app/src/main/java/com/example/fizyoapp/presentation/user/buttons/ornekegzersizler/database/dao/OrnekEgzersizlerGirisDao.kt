package com.example.fizyoapp.presentation.user.buttons.ornekegzersizler.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.fizyoapp.presentation.user.buttons.ornekegzersizler.database.entity.OrnekEgzersizlerGiris


@Dao
interface OrnekEgzersizlerGirisDao {
    @Query("SELECT * FROM ornekegzersizlergiris")
    fun getAll():List<OrnekEgzersizlerGiris>
}