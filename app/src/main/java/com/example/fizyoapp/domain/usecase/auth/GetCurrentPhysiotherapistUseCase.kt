package com.example.fizyoapp.domain.usecase.auth

import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.auth.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCurrentPhysiotherapistUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<User>> {
        return authRepository.getCurrentUser().map { resource ->
            when (resource) {
                is Resource.Success -> {
                    val user = resource.data.user
                    if (user != null && user.role == UserRole.PHYSIOTHERAPIST) {
                        Resource.Success(user)
                    } else {
                        Resource.Error("Fizyoterapist hesabı bulunamadı")
                    }
                }
                is Resource.Error -> Resource.Error(resource.message, resource.exception)
                is Resource.Loading -> Resource.Loading()
            }
        }
    }
}