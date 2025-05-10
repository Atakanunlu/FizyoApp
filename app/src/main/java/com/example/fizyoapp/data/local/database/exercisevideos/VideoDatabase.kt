package com.example.fizyoapp.data.local.database.exercisevideos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fizyoapp.data.local.dao.exercisevideos.VideoDao
import com.example.fizyoapp.data.local.entity.exercisevideos.ExamplesOfExercisesEntity


@Database(
    entities = [ExamplesOfExercisesEntity::class],
    version = 3
)
abstract class VideoDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao

    companion object {

        @Volatile
        private var INSTANCE: VideoDatabase? = null

        fun getDatabase(context: Context): VideoDatabase {
            // Varsa mevcut örneği, yoksa yenisini döndür
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VideoDatabase::class.java,
                    "video_database"
                )
                    .fallbackToDestructiveMigration() // Şema değişikliklerinde verileri sil
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}