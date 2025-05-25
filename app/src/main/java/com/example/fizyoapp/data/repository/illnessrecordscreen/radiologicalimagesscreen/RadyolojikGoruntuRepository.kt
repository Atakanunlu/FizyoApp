package com.example.fizyoapp.data.repository.illnessrecordscreen.radiologicalimagesscreen

import android.net.Uri
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.presentation.user.illnessrecord.radyologicalimagesadd.RadyolojikGoruntu
import kotlinx.coroutines.flow.Flow

interface RadyolojikGoruntuRepository {
    suspend fun getRadyolojikGoruntuler(userId: String): Flow<Resource<List<RadyolojikGoruntu>>>

    suspend fun uploadRadyolojikGoruntu(
        fileUri: Uri,
        title: String,
        description: String,
        userId: String,
        fileType: String = "image"
    ): Flow<Resource<RadyolojikGoruntu>>

    suspend fun deleteRadyolojikGoruntu(fileUrl: String): Flow<Resource<Boolean>>
}