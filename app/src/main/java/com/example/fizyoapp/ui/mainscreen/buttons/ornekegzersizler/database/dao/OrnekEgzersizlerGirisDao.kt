package com.example.fizyoapp.ui.mainscreen.buttons.ornekegzersizler.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.fizyoapp.ui.mainscreen.buttons.ornekegzersizler.database.entity.OrnekEgzersizlerGiris


@Dao
interface OrnekEgzersizlerGirisDao {
    @Query("SELECT * FROM ornekegzersizlergiris")
    fun getAll():List<OrnekEgzersizlerGiris>
}