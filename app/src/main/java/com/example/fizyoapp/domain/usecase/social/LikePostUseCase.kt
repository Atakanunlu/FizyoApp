package com.example.fizyoapp.domain.usecase.social

import com.example.fizyoapp.data.repository.social.SocialRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LikePostUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    operator fun invoke(postId: String): Flow<Resource<Boolean>> {
        return socialRepository.likePost(postId)
    }
}