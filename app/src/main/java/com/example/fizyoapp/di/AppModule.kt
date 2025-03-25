package com.example.fizyoapp.di

import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.auth.AuthRepositoryImpl
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.auth.GetUserRoleUseCase
import com.example.fizyoapp.domain.usecase.auth.SignInUseCase
import com.example.fizyoapp.domain.usecase.auth.SignOutUseCase
import com.example.fizyoapp.domain.usecase.auth.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideGetCurrentUseCase(authRepository: AuthRepository): GetCurrentUseCase {
        return GetCurrentUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideGetUserRoleUseCase(authRepository: AuthRepository): GetUserRoleUseCase {
        return GetUserRoleUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSignInUseCase(authRepository: AuthRepository): SignInUseCase {
        return SignInUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSignUpUseCase(authRepository: AuthRepository): SignUpUseCase {
        return SignUpUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSignOutUseCase(authRepository: AuthRepository): SignOutUseCase {
        return SignOutUseCase(authRepository)
    }
}