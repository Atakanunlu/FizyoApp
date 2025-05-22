package com.example.fizyoapp.domain.usecase.socialmedia

import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepository
import com.example.fizyoapp.domain.model.socialmedia.Comment
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val repository: SocialMediaRepository
) {
    operator fun invoke(comment: Comment) = repository.addComment(comment)
}