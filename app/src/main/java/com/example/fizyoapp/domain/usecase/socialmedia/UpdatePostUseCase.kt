// domain/usecase/socialmedia/UpdatePostUseCase.kt
package com.example.fizyoapp.domain.usecase.socialmedia

import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepository
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdatePostUseCase @Inject constructor(
    private val repository: SocialMediaRepository
) {
    operator fun invoke(
        postId: String,
        content: String,
        existingMediaUrls: List<String>,
        existingMediaTypes: List<String>,
        newMediaUris: List<String>
    ): Flow<Resource<Post>> = repository.updatePost(
        postId, content, existingMediaUrls, existingMediaTypes, newMediaUris
    )
}