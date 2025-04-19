package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.repository

import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.model.ExamplesOfExercisesEntity
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.model.VideoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExamplesOfExercisesRepositoryImp(private val videoDao: VideoDao): ExamplesOfExerciseRepository {
    override suspend fun insertVideo(video: ExamplesOfExercisesEntity) {
        withContext(Dispatchers.IO) {
            videoDao.insertVideo(video)
        }
    }

    override suspend fun insertVideos(videos: List<ExamplesOfExercisesEntity>) {
        withContext(Dispatchers.IO) {
            videoDao.insertVideos(videos)
        }
    }

    override fun getAllVideo(): Flow<List<ExamplesOfExercisesEntity>> {
        return videoDao.getAllVideos()
    }

    override suspend fun deleteAllVideos() {
        withContext(Dispatchers.IO) {
            videoDao.deleteAllVideos()
        }
    }
    override suspend fun insertVideoWithCategory(video: ExamplesOfExercisesEntity, category: String) {
        withContext(Dispatchers.IO) {
            val videoWithCategory = video.copy(category = category)
            videoDao.insertVideo(videoWithCategory)
        }
    }

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

