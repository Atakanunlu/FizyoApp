package com.example.fizyoapp.data.model

import com.example.fizyoapp.domain.model.User
import com.example.fizyoapp.domain.model.UserRole

sealed class AuthResult {
    data class SignInResult(val user: User): AuthResult()
    data class SignUpResult(val user: User): AuthResult()
    data class CurrentUserResult(val user: User?): AuthResult()
    data class UserRoleResult(val role: UserRole?): AuthResult()
    data object SignOutResult: AuthResult()
}