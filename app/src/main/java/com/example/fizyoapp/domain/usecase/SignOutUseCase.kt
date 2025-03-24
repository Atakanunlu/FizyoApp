package com.example.fizyoapp.domain.usecase

import com.example.fizyoapp.data.repository.AuthRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<Unit>>{
        return authRepository.signOut().map { resource ->
            when(resource){
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(resource.message,resource.exception)
                is Resource.Loading -> Resource.Loading()
            }
        }
    }
}