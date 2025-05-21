package com.example.fizyoapp.domain.usecase.social

import com.example.fizyoapp.data.repository.social.SocialRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.social.SocialProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFollowingUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    operator fun invoke(userId: String): Flow<Resource<List<SocialProfile>>> {
        return socialRepository.getFollowing(userId)
    }
}