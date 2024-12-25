package com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase

@Database(entities = [FizyoterapistlerEntity::class], version = 1)
abstract class FizyoterapistDatabase : RoomDatabase() {
    abstract fun fizyolistDao(): FizyoterapistDao

    companion object {
        @Volatile
        private var INSTANCE: FizyoterapistDatabase? = null

        fun getDatabase(context: Context): FizyoterapistDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = databaseBuilder(
                    context.applicationContext,
                    FizyoterapistDatabase::class.java,
                    "fizyolist_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}