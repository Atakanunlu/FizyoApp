package com.example.fizyoapp.domain.usecase.physiotherapist_profile

import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckPhysiotherapistProfileCompletedUseCase @Inject constructor(
    private val repository: PhysiotherapistProfileRepository
) {
    operator fun invoke(userId: String): Flow<Resource<Boolean>> {
        return repository.checkProfileCompleted(userId)
    }
}