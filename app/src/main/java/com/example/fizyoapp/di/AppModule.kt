package com.example.fizyoapp.di


import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.auth.AuthRepositoryImpl
import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepository
import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepositoryImpl
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepositoryImpl
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.auth.GetUserRoleUseCase
import com.example.fizyoapp.domain.usecase.auth.SignInUseCase
import com.example.fizyoapp.domain.usecase.auth.SignOutUseCase
import com.example.fizyoapp.domain.usecase.auth.SignUpUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.CheckPhysiotherapistProfileCompletedUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetAllPhysiotherapistsUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.UpdatePhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.UploadPhysiotherapistProfilePhotoUseCase
import com.example.fizyoapp.domain.usecase.user_profile.CheckProfileCompletedUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import com.example.fizyoapp.domain.usecase.user_profile.UpdateUserProfileUseCase
import com.example.fizyoapp.domain.usecase.user_profile.UploadProfilePhotoUseCase
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
    fun provideUserProfileRepository(): UserProfileRepository {
        return UserProfileRepositoryImpl()
    }

    @Provides
    @Singleton
    fun providePhysiotherapistProfileRepository(): PhysiotherapistProfileRepository {
        return PhysiotherapistProfileRepositoryImpl()
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

    @Provides
    @Singleton
    fun provideGetUserProfileUseCase(userProfileRepository: UserProfileRepository): GetUserProfileUseCase {
        return GetUserProfileUseCase(userProfileRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateUserProfileUseCase(userProfileRepository: UserProfileRepository): UpdateUserProfileUseCase {
        return UpdateUserProfileUseCase(userProfileRepository)
    }

    @Provides
    @Singleton
    fun provideCheckProfileCompletedUseCase(userProfileRepository: UserProfileRepository): CheckProfileCompletedUseCase {
        return CheckProfileCompletedUseCase(userProfileRepository)
    }

    @Provides
    @Singleton
    fun provideUploadProfilePhotoUseCase(userProfileRepository: UserProfileRepository): UploadProfilePhotoUseCase {
        return UploadProfilePhotoUseCase(userProfileRepository)
    }

    @Provides
    @Singleton
    fun provideGetPhysiotherapistProfileUseCase(repository: PhysiotherapistProfileRepository): GetPhysiotherapistProfileUseCase {
        return GetPhysiotherapistProfileUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdatePhysiotherapistProfileUseCase(repository: PhysiotherapistProfileRepository): UpdatePhysiotherapistProfileUseCase {
        return UpdatePhysiotherapistProfileUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCheckPhysiotherapistProfileCompletedUseCase(repository: PhysiotherapistProfileRepository): CheckPhysiotherapistProfileCompletedUseCase {
        return CheckPhysiotherapistProfileCompletedUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUploadPhysiotherapistProfilePhotoUseCase(repository: PhysiotherapistProfileRepository): UploadPhysiotherapistProfilePhotoUseCase {
        return UploadPhysiotherapistProfilePhotoUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetAllPhysiotherapistsUseCase(repository: PhysiotherapistProfileRepository): GetAllPhysiotherapistsUseCase {
        return GetAllPhysiotherapistsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetPhysiotherapistByIdUseCase(repository: PhysiotherapistProfileRepository): GetPhysiotherapistByIdUseCase {
        return GetPhysiotherapistByIdUseCase(repository)
    }

}
