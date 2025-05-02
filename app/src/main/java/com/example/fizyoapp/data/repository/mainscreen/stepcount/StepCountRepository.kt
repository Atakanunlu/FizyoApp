package com.example.fizyoapp.data.repository.mainscreen.stepcount

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.StepCount
import kotlinx.coroutines.flow.Flow

interface StepCountRepository {
    suspend fun updateStepCount(stepCount: StepCount): Resource<Unit>
    fun getStepCountForToday(userId: String): Flow<Resource<StepCount?>>
    fun getStepCountHistory(userId: String, limit: Int): Flow<Resource<List<StepCount>>>
}