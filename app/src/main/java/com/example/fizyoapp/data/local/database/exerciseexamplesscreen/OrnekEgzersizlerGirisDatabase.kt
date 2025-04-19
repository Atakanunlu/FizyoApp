package com.example.fizyoapp.presentation.user.ornekegzersizler.database.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fizyoapp.presentation.user.ornekegzersizler.database.dao.OrnekEgzersizlerGirisDao
import com.example.fizyoapp.presentation.user.ornekegzersizler.database.entity.OrnekEgzersizlerGiris


@Database(entities = arrayOf(OrnekEgzersizlerGiris::class), version = 1, exportSchema = false)
abstract class  OrnekEgzersizlerGirisDatabase:RoomDatabase() {
    abstract fun OrnekEgzersizlerGirisDao(): OrnekEgzersizlerGirisDao



}
