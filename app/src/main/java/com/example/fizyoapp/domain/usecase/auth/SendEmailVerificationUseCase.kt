package com.example.fizyoapp.domain.usecase.auth

import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SendEmailVerificationUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<Boolean>> {
        return authRepository.sendEmailVerification().map { resource ->
            when (resource) {
                is Resource.Success -> Resource.Success(resource.data.isEmailSent)
                is Resource.Error -> Resource.Error(resource.message, resource.exception)
                is Resource.Loading -> Resource.Loading()
            }
        }
    }
}