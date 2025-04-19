package com.example.fizyoapp.data.repository.exercisevideos

import com.example.fizyoapp.data.local.entity.exercisevideos.ExamplesOfExercisesEntity
import com.example.fizyoapp.data.local.dao.exercisevideos.VideoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * ExamplesOfExercisesRepositoryImp: Video repository arayüzünün somut uygulaması
 *
 * Bu sınıf:
 * 1. Repository arayüzünün sadece ihtiyaç duyulan metodlarını uygular
 * 2. DAO ile etkileşimi Dispatcher.IO thread'inde gerçekleştirir
 * 3. UI thread'inde veritabanı işlemlerinin yapılmasını önler
 *
 * @param videoDao: Video veritabanı işlemlerini gerçekleştiren DAO nesnesi
 */

class ExamplesOfExercisesRepositoryImp(private val videoDao: VideoDao):
    ExamplesOfExerciseRepository {

    override suspend fun insertVideos(videos: List<ExamplesOfExercisesEntity>) {
        withContext(Dispatchers.IO) {
            videoDao.insertVideos(videos)
        }
    }

    /**
     * Kategori bazlı video sorgulama
     *
     * @param category: Filtrelenecek kategori
     * @return: Flow nesnesi olarak filtrelenmiş videolar
     *
     * Not: Flow, Dispatchers.IO context'inde otomatik olarak çalışır,
     * bu nedenle withContext kullanmaya gerek yoktur.
     */

    // Kategori bazlı video sorgulama
    override fun getVideosByCategory(category: String): Flow<List<ExamplesOfExercisesEntity>> {
        return videoDao.getVideosByCategory(category)
    }

    // Kategori bazlı silme işlemi
    override suspend fun deleteVideosByCategory(category: String) {
        withContext(Dispatchers.IO) {
            videoDao.deleteVideosByCategory(category)
        }
    }
}

