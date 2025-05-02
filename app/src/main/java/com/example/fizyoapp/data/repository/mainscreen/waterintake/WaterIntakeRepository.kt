package com.example.fizyoapp.data.repository.mainscreen.waterintake

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.usermainscreen.WaterIntake
import kotlinx.coroutines.flow.Flow

interface WaterIntakeRepository {
    suspend fun updateWaterIntake(waterIntake: WaterIntake): Resource<Unit>
    fun getWaterIntakeForToday(userId: String): Flow<Resource<WaterIntake?>>
    fun getWaterIntakeHistory(userId: String, limit: Int): Flow<Resource<List<WaterIntake>>>
}