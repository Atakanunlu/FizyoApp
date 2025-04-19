package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fizyoapp.data.local.entity.ExamplesOfExercisesEntity

@Database(entities = [ExamplesOfExercisesEntity::class], version = 2)
abstract class VideoDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao

    companion object {
        @Volatile
        private var INSTANCE: VideoDatabase? = null

        fun getDatabase(context: Context) : VideoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VideoDatabase::class.java,
                    "video_database"
                )
                    // .allowMainThreadQueries() KALDIRIN BU SATIRI
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}