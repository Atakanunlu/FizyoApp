package com.example.fizyoapp.data.local.entity.auth

import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.auth.UserRole

sealed class AuthResult {
    data class SignInResult(val user: User) : AuthResult()
    data class SignUpResult(val user: User) : AuthResult()
    data object SignOutResult : AuthResult()
    data class CurrentUserResult(val user: User?) : AuthResult()
    data class UserRoleResult(val role: UserRole?) : AuthResult()
    data class EmailVerificationResult(val isEmailSent: Boolean = false) : AuthResult()
    data class EmailVerifiedResult(val isEmailVerified: Boolean = false) : AuthResult()
    data class PasswordResetEmailResult(val isEmailSent: Boolean = false) : AuthResult()
    data class VerifyResetCodeResult(val isCodeValid: Boolean = false) : AuthResult()
    data class ResetPasswordResult(val isSuccess: Boolean = false) : AuthResult()
}