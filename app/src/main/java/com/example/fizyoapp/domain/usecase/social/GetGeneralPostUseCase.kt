package com.example.fizyoapp.domain.usecase.social

import com.example.fizyoapp.data.repository.social.SocialRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.social.Post
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGeneralPostsUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    operator fun invoke(): Flow<Resource<List<Post>>> {
        return socialRepository.getGeneralPosts()
    }
}