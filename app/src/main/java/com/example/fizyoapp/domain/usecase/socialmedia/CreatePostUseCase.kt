// domain/usecase/socialmedia/CreatePostUseCase.kt
package com.example.fizyoapp.domain.usecase.socialmedia

import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepository
import com.example.fizyoapp.domain.model.socialmedia.Post
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val repository: SocialMediaRepository
) {
    operator fun invoke(post: Post, mediaUris: List<String>) =
        repository.createPost(post, mediaUris)
}