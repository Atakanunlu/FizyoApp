package com.example.fizyoapp.domain.usecase.user_profile

import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    operator fun invoke(userId: String): Flow<Resource<UserProfile>> {
        return userProfileRepository.getUserProfile(userId)
    }
}