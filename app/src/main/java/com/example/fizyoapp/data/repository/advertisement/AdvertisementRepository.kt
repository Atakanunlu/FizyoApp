package com.example.fizyoapp.data.repository.advertisement

import android.net.Uri
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.advertisement.Advertisement
import kotlinx.coroutines.flow.Flow

interface AdvertisementRepository {
    fun getActiveAdvertisements(): Flow<Resource<List<Advertisement>>>
    fun getAdvertisementById(id: String): Flow<Resource<Advertisement>>
    fun getAdvertisementsByPhysiotherapistId(physiotherapistId: String): Flow<Resource<List<Advertisement>>>
    fun createAdvertisement(
        physiotherapistId: String,
        imageUri: Uri,
        description: String,
        paymentId: String
    ): Flow<Resource<Advertisement>>
    fun deleteAdvertisement(id: String): Flow<Resource<Boolean>>
    fun checkActiveAdvertisementByPhysiotherapist(physiotherapistId: String): Flow<Resource<Boolean>>
}