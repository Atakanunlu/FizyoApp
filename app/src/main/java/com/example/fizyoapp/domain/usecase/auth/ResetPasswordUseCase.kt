package com.example.fizyoapp.domain.usecase.auth

import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(code: String, newPassword: String): Flow<Resource<Boolean>> {
        return authRepository.resetPassword(code, newPassword).map { resource ->
            when (resource) {
                is Resource.Success -> Resource.Success(resource.data.isSuccess)
                is Resource.Error -> Resource.Error(resource.message, resource.exception)
                is Resource.Loading -> Resource.Loading()
            }
        }
    }
}