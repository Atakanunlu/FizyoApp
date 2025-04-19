package com.example.fizyoapp.domain.usecase.user_profile

import android.util.Log
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class CheckProfileCompletedUseCase @Inject constructor(
    private val repository: UserProfileRepository
) {
    operator fun invoke(userId: String): Flow<Resource<Boolean>> = flow {
        try {
            // Önce repository'den veri almayı dene
            repository.checkProfileCompleted(userId).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            Log.e("CheckProfileCompletedUseCase", "Error checking profile, defaulting to completed", e)
            emit(Resource.Success(true))
        }
    }
}