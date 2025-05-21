// domain/usecase/socialmedia/DeleteCommentUseCase.kt
package com.example.fizyoapp.domain.usecase.socialmedia

import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepository
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val repository: SocialMediaRepository
) {
    operator fun invoke(commentId: String) = repository.deleteComment(commentId)
}