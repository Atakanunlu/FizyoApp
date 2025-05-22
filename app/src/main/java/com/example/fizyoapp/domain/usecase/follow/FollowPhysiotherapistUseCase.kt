package com.example.fizyoapp.domain.usecase.follow

import com.example.fizyoapp.data.repository.follow.FollowRepository
import javax.inject.Inject

class FollowPhysiotherapistUseCase @Inject constructor(
    private val repository: FollowRepository
) {
    operator fun invoke(followerId: String, followerRole: String, followedId: String) =
        repository.followPhysiotherapist(followerId, followerRole, followedId)
}
