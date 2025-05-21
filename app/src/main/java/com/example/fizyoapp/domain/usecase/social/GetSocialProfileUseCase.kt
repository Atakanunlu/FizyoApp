package com.example.fizyoapp.domain.usecase.social

import com.example.fizyoapp.data.repository.social.SocialRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.social.SocialProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSocialProfileUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    operator fun invoke(userId: String): Flow<Resource<SocialProfile>> {
        return socialRepository.getSocialProfile(userId)
    }
}