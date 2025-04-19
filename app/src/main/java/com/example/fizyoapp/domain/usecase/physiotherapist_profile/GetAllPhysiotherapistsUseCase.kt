package com.example.fizyoapp.domain.usecase.physiotherapist_profile

import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPhysiotherapistsUseCase @Inject constructor(
    private val repository: PhysiotherapistProfileRepository
) {
    operator fun invoke(): Flow<Resource<List<PhysiotherapistProfile>>> {
        return repository.getAllPhysiotherapists()
    }
}