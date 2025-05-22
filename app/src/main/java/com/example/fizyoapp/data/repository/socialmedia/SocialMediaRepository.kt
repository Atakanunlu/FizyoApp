package com.example.fizyoapp.data.repository.socialmedia

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.socialmedia.Comment
import com.example.fizyoapp.domain.model.socialmedia.Post
import kotlinx.coroutines.flow.Flow

interface SocialMediaRepository {
    fun getAllPosts(): Flow<Resource<List<Post>>>
    fun createPost(post: Post, mediaUris: List<String>): Flow<Resource<Post>>
    fun getPostById(postId: String): Flow<Resource<Post>>
    fun likePost(postId: String, userId: String): Flow<Resource<Unit>>
    fun unlikePost(postId: String, userId: String): Flow<Resource<Unit>>
    fun getCommentsByPostId(postId: String): Flow<Resource<List<Comment>>>
    fun addComment(comment: Comment): Flow<Resource<Comment>>
    fun deletePost(postId: String): Flow<Resource<Unit>>
    fun updatePost(
        postId: String,
        content: String,
        existingMediaUrls: List<String>,
        existingMediaTypes: List<String>,
        newMediaUris: List<String>
    ): Flow<Resource<Post>>

    fun deleteComment(commentId: String): Flow<Resource<Unit>>
}