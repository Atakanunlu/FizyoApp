package com.example.fizyoapp.domain.usecase.auth

import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class VerifyPasswordResetCodeUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(code: String): Flow<Resource<Boolean>> {
        return authRepository.verifyPasswordResetCode(code).map { resource ->
            when (resource) {
                is Resource.Success -> Resource.Success(resource.data.isCodeValid)
                is Resource.Error -> Resource.Error(resource.message, resource.exception)
                is Resource.Loading -> Resource.Loading()
            }
        }
    }
}