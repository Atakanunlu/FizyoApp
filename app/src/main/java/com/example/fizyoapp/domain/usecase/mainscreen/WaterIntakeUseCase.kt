package com.example.fizyoapp.domain.usecase.mainscreen
import com.example.fizyoapp.data.repository.mainscreen.waterintake.WaterIntakeRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.WaterIntake
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWaterIntakeForTodayUseCase @Inject constructor(
    private val waterIntakeRepository: WaterIntakeRepository
) {
    operator fun invoke(userId: String): Flow<Resource<WaterIntake?>> {
        return waterIntakeRepository.getWaterIntakeForToday(userId)
    }
}

class UpdateWaterIntakeUseCase @Inject constructor(
    private val waterIntakeRepository: WaterIntakeRepository
) {
    suspend operator fun invoke(waterIntake: WaterIntake): Resource<Unit> {
        return waterIntakeRepository.updateWaterIntake(waterIntake)
    }
}