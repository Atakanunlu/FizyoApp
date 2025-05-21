// domain/usecase/socialmedia/GetCommentsByPostIdUseCase.kt
package com.example.fizyoapp.domain.usecase.socialmedia

import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepository
import javax.inject.Inject

class GetCommentsByPostIdUseCase @Inject constructor(
    private val repository: SocialMediaRepository
) {
    operator fun invoke(postId: String) = repository.getCommentsByPostId(postId)
}