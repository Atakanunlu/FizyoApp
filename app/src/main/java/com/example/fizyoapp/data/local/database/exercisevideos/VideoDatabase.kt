package com.example.fizyoapp.data.local.database.exercisevideos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fizyoapp.data.local.dao.exercisevideos.VideoDao
import com.example.fizyoapp.data.local.entity.exercisevideos.ExamplesOfExercisesEntity

/**
 * VideoDatabase: Room veritabanı sınıfı
 *
 * Bu sınıf:
 * 1. SQLite veritabanını oluşturur ve yönetir
 * 2. Singleton deseni ile tek bir veritabanı örneği sağlar
 * 3. DAO erişimini sağlayan metodu tanımlar
 */
@Database(
    entities = [ExamplesOfExercisesEntity::class], // Veritabanı tabloları
    version = 3                                   // Veritabanı şema versiyonu
)
abstract class VideoDatabase : RoomDatabase() {
    /**
     * VideoDao nesnesine erişim sağlayan soyut metot
     * Room, bu metodun uygulamasını otomatik olarak oluşturur
     */
    abstract fun videoDao(): VideoDao

    companion object {
        /**
         * Veritabanı singleton örneği
         * @Volatile işaretlemesi, farklı thread'ler arasında tutarlılık sağlar
         */
        @Volatile
        private var INSTANCE: VideoDatabase? = null

        /**
         * Veritabanı örneğini döndüren metot (Singleton pattern)
         *
         * @param context: Veritabanı oluşturmak için Android context'i
         * @return: VideoDatabase örneği
         */
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