// domain/usecase/socialmedia/GetPostByIdUseCase.kt
package com.example.fizyoapp.domain.usecase.socialmedia

import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepository
import javax.inject.Inject

class GetPostByIdUseCase @Inject constructor(
    private val repository: SocialMediaRepository
) {
    operator fun invoke(postId: String) = repository.getPostById(postId)
}