
package com.example.fizyoapp.data.repository.physiotherapist_profile

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import kotlinx.coroutines.flow.Flow

interface PhysiotherapistProfileRepository {
    fun getPhysiotherapistProfile(userId: String): Flow<Resource<PhysiotherapistProfile>>
    fun updatePhysiotherapistProfile(profile: PhysiotherapistProfile): Flow<Resource<PhysiotherapistProfile>>
    fun checkProfileCompleted(userId: String): Flow<Resource<Boolean>>
    fun uploadProfilePhoto(photoUriString: String, userId: String): Flow<Resource<String>>
    fun getAllPhysiotherapists(): Flow<Resource<List<PhysiotherapistProfile>>>
    fun getPhysiotherapistById(physiotherapistId: String): Flow<Resource<PhysiotherapistProfile>>
}