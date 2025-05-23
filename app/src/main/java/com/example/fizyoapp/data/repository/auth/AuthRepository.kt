package com.example.fizyoapp.data.repository.auth

import com.example.fizyoapp.data.local.entity.auth.AuthResult
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
    fun sendEmailVerification(): Flow<Resource<AuthResult.EmailVerificationResult>>
    fun checkEmailVerified(): Flow<Resource<AuthResult.EmailVerifiedResult>>
    fun sendPasswordResetEmail(email: String): Flow<Resource<AuthResult.PasswordResetEmailResult>>
    fun verifyPasswordResetCode(code: String): Flow<Resource<AuthResult.VerifyResetCodeResult>>
    fun resetPassword(code: String, newPassword: String): Flow<Resource<AuthResult.ResetPasswordResult>>
}