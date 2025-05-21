// domain/usecase/socialmedia/UnlikePostUseCase.kt
package com.example.fizyoapp.domain.usecase.socialmedia

import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepository
import javax.inject.Inject

class UnlikePostUseCase @Inject constructor(
    private val repository: SocialMediaRepository
) {
    operator fun invoke(postId: String, userId: String) =
        repository.unlikePost(postId, userId)
}