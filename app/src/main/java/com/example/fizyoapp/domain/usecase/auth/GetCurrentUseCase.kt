package com.example.fizyoapp.domain.usecase.auth

import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCurrentUseCase @Inject constructor(
    private val authRepository: AuthRepository
){
    operator fun invoke(): Flow<Resource<User?>> {
        return authRepository.getCurrentUser().map { resource ->
            when(resource){
                is Resource.Success -> Resource.Success(resource.data.user)
                is Resource.Error -> Resource.Error(resource.message,resource.exception)
                is Resource.Loading -> Resource.Loading()
            }
        }
    }
}