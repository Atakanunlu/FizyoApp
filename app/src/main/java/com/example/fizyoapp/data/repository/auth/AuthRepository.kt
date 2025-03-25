package com.example.fizyoapp.data.repository.auth

import com.example.fizyoapp.data.model.auth.AuthResult
import com.example.fizyoapp.data.util.Resource
import com.example.fizyoapp.domain.model.auth.UserRole

import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun signIn(email: String, password: String, role: UserRole): Flow<Resource<AuthResult.SignInResult>>

    fun signUp(email: String, password: String, role: UserRole): Flow<Resource<AuthResult.SignUpResult>>

    fun signOut(): Flow<Resource<AuthResult.SignOutResult>>

    fun getCurrentUser(): Flow<Resource<AuthResult.CurrentUserResult>>

    fun getUserRole(userId: String): Flow<Resource<AuthResult.UserRoleResult>>

    fun observeAuthState(): Flow<Resource<AuthResult.CurrentUserResult>>


}