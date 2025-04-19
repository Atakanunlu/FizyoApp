package com.example.fizyoapp.data.local.database.exerciseexamplesscreen

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fizyoapp.data.local.dao.exerciseexamplesscreen.OrnekEgzersizlerGirisDao
import com.example.fizyoapp.data.local.entity.exerciseexamplesscreen.OrnekEgzersizlerGiris

@Database(entities = [OrnekEgzersizlerGiris::class], version = 2, exportSchema = false)
abstract class ExercisesDatabase : RoomDatabase() {
    abstract fun exerciseCategoryDao(): OrnekEgzersizlerGirisDao

    companion object {
        @Volatile
        private var INSTANCE: ExercisesDatabase? = null

        fun getInstance(context: Context): ExercisesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExercisesDatabase::class.java,
                    "exercises_database"

                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}