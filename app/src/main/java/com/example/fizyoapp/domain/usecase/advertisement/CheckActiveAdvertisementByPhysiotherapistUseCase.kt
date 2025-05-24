package com.example.fizyoapp.domain.usecase.advertisement

import com.example.fizyoapp.data.repository.advertisement.AdvertisementRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckActiveAdvertisementByPhysiotherapistUseCase @Inject constructor(
    private val advertisementRepository: AdvertisementRepository
) {
    operator fun invoke(physiotherapistId: String): Flow<Resource<Boolean>> {
        return advertisementRepository.checkActiveAdvertisementByPhysiotherapist(physiotherapistId)
    }
}