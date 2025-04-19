package com.example.fizyoapp.data.repository.user_profile

import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getUserProfile(userId: String): Flow<Resource<UserProfile>>
    fun updateUserProfile(userProfile: UserProfile): Flow<Resource<UserProfile>>
    fun checkProfileCompleted(userId: String): Flow<Resource<Boolean>>
    fun uploadProfilePhoto(photoUriString: String, userId: String): Flow<Resource<String>>
}