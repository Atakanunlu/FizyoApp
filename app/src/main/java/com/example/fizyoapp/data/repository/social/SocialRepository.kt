package com.example.fizyoapp.data.repository.social

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.social.Comment
import com.example.fizyoapp.domain.model.social.Post
import com.example.fizyoapp.domain.model.social.SocialProfile
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    fun getGeneralPosts(): Flow<Resource<List<Post>>>
    fun getFollowingPosts(): Flow<Resource<List<Post>>>
    fun getUserPosts(userId: String): Flow<Resource<List<Post>>>
    fun createPost(content: String, mediaUris: List<String>): Flow<Resource<Post>>
    fun likePost(postId: String): Flow<Resource<Boolean>>
    fun unlikePost(postId: String): Flow<Resource<Boolean>>
    fun getComments(postId: String): Flow<Resource<List<Comment>>>
    fun addComment(postId: String, content: String): Flow<Resource<Comment>>
    fun getSocialProfile(userId: String): Flow<Resource<SocialProfile>>
    fun followUser(userId: String): Flow<Resource<Boolean>>
    fun unfollowUser(userId: String): Flow<Resource<Boolean>>
    fun getFollowers(userId: String): Flow<Resource<List<SocialProfile>>>
    fun getFollowing(userId: String): Flow<Resource<List<SocialProfile>>>
}