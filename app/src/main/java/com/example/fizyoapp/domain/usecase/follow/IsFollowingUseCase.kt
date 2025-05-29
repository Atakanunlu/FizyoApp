package com.example.fizyoapp.domain.usecase.follow

import com.example.fizyoapp.data.repository.follow.FollowRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class IsFollowingUseCase @Inject constructor(
    private val repository: FollowRepository
) {
    operator fun invoke(followerId: String, followedId: String): Flow<Resource<Boolean>> {
        return repository.isFollowing(followerId, followedId)
            .onStart { emit(Resource.Loading()) }
            .catch { e -> emit(Resource.Error(e.message ?: "Takip durumu kontrol edilemedi")) }
    }
}