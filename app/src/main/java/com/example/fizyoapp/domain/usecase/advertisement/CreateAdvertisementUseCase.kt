package com.example.fizyoapp.domain.usecase.advertisement

import android.net.Uri
import com.example.fizyoapp.data.repository.advertisement.AdvertisementRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.advertisement.Advertisement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateAdvertisementUseCase @Inject constructor(
    private val advertisementRepository: AdvertisementRepository
) {
    operator fun invoke(
        physiotherapistId: String,
        imageUri: Uri,
        description: String,
        paymentId: String
    ): Flow<Resource<Advertisement>> {
        return advertisementRepository.createAdvertisement(
            physiotherapistId, imageUri, description, paymentId
        )
    }
}