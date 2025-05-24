package com.example.fizyoapp.domain.usecase.advertisement

import com.example.fizyoapp.data.repository.advertisement.AdvertisementRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.advertisement.Advertisement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveAdvertisementsUseCase @Inject constructor(
    private val advertisementRepository: AdvertisementRepository
) {
    operator fun invoke(): Flow<Resource<List<Advertisement>>> {
        return advertisementRepository.getActiveAdvertisements()
    }
}