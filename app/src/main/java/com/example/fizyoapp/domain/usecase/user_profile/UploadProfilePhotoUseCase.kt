package com.example.fizyoapp.domain.usecase.user_profile

import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UploadProfilePhotoUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    operator fun invoke(photoUriString: String, userId: String): Flow<Resource<String>> {
        return userProfileRepository.uploadProfilePhoto(photoUriString, userId)
    }
}