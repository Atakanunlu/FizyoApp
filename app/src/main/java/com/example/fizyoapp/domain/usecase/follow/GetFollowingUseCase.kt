package com.example.fizyoapp.domain.usecase.follow

import com.example.fizyoapp.data.repository.follow.FollowRepository
import javax.inject.Inject

class GetFollowingUseCase @Inject constructor(
    private val repository: FollowRepository
) {
    operator fun invoke(userId: String) =
        repository.getFollowing(userId)
}