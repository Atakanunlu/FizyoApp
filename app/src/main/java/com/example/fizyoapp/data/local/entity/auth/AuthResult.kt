package com.example.fizyoapp.data.model.auth

import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.domain.model.auth.UserRole

sealed class AuthResult {
    data class SignInResult(val user: User): AuthResult()
    data class SignUpResult(val user: User): AuthResult()
    data class CurrentUserResult(val user: User?): AuthResult()
    data class UserRoleResult(val role: UserRole?): AuthResult()
    data object SignOutResult: AuthResult()
}