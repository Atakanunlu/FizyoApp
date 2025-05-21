package com.example.fizyoapp.data.repository.exercisevideos

import com.example.fizyoapp.data.local.entity.exercisevideos.ExamplesOfExercisesEntity
import com.example.fizyoapp.data.local.dao.exercisevideos.VideoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExamplesOfExercisesRepositoryImp(private val videoDao: VideoDao):
    ExamplesOfExerciseRepository {

    override suspend fun insertVideos(videos: List<ExamplesOfExercisesEntity>) {
        withContext(Dispatchers.IO) {
            videoDao.insertVideos(videos)
        }
    }

    override fun getVideosByCategory(category: String): Flow<List<ExamplesOfExercisesEntity>> {
        return videoDao.getVideosByCategory(category)
    }

    override suspend fun deleteVideosByCategory(category: String) {
        withContext(Dispatchers.IO) {
            videoDao.deleteVideosByCategory(category)
        }
    }
}

