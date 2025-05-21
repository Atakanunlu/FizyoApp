package com.example.fizyoapp.domain.usecase.social

import com.example.fizyoapp.data.repository.social.SocialRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FollowUserUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    operator fun invoke(userId: String): Flow<Resource<Boolean>> {
        return socialRepository.followUser(userId)
    }
}