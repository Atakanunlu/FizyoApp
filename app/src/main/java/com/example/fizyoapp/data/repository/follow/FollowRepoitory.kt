package com.example.fizyoapp.data.repository.follow

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.follow.FollowRelation
import kotlinx.coroutines.flow.Flow

interface FollowRepository {
    fun followPhysiotherapist(followerId: String, followerRole: String, followedId: String): Flow<Resource<Unit>>
    fun unfollowPhysiotherapist(followerId: String, followedId: String): Flow<Resource<Unit>>
    fun isFollowing(followerId: String, followedId: String): Flow<Resource<Boolean>>
    fun getFollowersCount(physiotherapistId: String): Flow<Resource<Int>>
    fun getFollowingCount(userId: String): Flow<Resource<Int>>
    fun getFollowers(physiotherapistId: String): Flow<Resource<List<FollowRelation>>>
    fun getFollowing(userId: String): Flow<Resource<List<FollowRelation>>>
}