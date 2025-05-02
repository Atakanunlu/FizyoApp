package com.example.fizyoapp.domain.usecase.mainscreen


import com.example.fizyoapp.data.repository.mainscreen.stepcount.StepCountRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.StepCount
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetStepCountForTodayUseCase @Inject constructor(
    private val stepCountRepository: StepCountRepository
) {
    operator fun invoke(userId: String): Flow<Resource<StepCount?>> {
        return stepCountRepository.getStepCountForToday(userId)
    }
}

class UpdateStepCountUseCase @Inject constructor(
    private val stepCountRepository: StepCountRepository
) {
    suspend operator fun invoke(stepCount: StepCount): Resource<Unit> {
        return stepCountRepository.updateStepCount(stepCount)
    }
}