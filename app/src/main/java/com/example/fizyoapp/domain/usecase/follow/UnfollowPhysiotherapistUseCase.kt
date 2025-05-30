package com.example.fizyoapp.domain.usecase.follow

import com.example.fizyoapp.data.repository.follow.FollowRepository
import javax.inject.Inject

class UnfollowPhysiotherapistUseCase @Inject constructor(
    private val repository: FollowRepository
) {
    operator fun invoke(followerId: String, followedId: String) =
        repository.unfollowPhysiotherapist(followerId, followedId)
}