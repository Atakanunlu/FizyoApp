// domain/usecase/socialmedia/LikePostUseCase.kt
package com.example.fizyoapp.domain.usecase.socialmedia

import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepository
import javax.inject.Inject

class LikePostUseCase @Inject constructor(
    private val repository: SocialMediaRepository
) {
    operator fun invoke(postId: String, userId: String) =
        repository.likePost(postId, userId)
}