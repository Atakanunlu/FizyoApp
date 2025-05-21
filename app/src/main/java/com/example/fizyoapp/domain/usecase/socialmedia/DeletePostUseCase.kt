// domain/usecase/socialmedia/DeletePostUseCase.kt
package com.example.fizyoapp.domain.usecase.socialmedia

import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepository
import javax.inject.Inject

class DeletePostUseCase @Inject constructor(
    private val repository: SocialMediaRepository
) {
    operator fun invoke(postId: String) = repository.deletePost(postId)
}