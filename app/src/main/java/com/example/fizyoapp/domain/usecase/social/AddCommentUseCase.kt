package com.example.fizyoapp.domain.usecase.social

import com.example.fizyoapp.data.repository.social.SocialRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.social.Comment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val socialRepository: SocialRepository
) {
    operator fun invoke(postId: String, content: String): Flow<Resource<Comment>> {
        return socialRepository.addComment(postId, content)
    }
}