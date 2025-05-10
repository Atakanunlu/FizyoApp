package com.example.fizyoapp.di

import android.content.Context
import com.example.fizyoapp.data.local.dao.exerciseexamplesscreen.OrnekEgzersizlerGirisDao
import com.example.fizyoapp.data.local.dao.exercisevideos.VideoDao
import com.example.fizyoapp.data.local.database.exerciseexamplesscreen.ExercisesDatabase
import com.example.fizyoapp.data.local.database.exercisevideos.VideoDatabase
import com.example.fizyoapp.data.repository.ExercisesExamplesRepositoryImpl
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.auth.AuthRepositoryImpl
import com.example.fizyoapp.data.repository.exercisesexamplesscreen.ExercisesExamplesRepository
import com.example.fizyoapp.data.repository.exercisevideos.ExamplesOfExerciseRepository
import com.example.fizyoapp.data.repository.exercisevideos.ExamplesOfExercisesRepositoryImp
import com.example.fizyoapp.data.repository.mainscreen.painrecord.PainTrackingRepository
import com.example.fizyoapp.data.repository.mainscreen.painrecord.PainTrackingRepositoryImpl
import com.example.fizyoapp.data.repository.messagesscreen.MessageRepository
import com.example.fizyoapp.data.repository.messagesscreen.MessageRepositoryImpl
import com.example.fizyoapp.data.repository.note.NoteRepository
import com.example.fizyoapp.data.repository.note.NoteRepositoryImpl
import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepository
import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepositoryImpl
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepositoryImpl
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.auth.GetUserRoleUseCase
import com.example.fizyoapp.domain.usecase.auth.SignInUseCase
import com.example.fizyoapp.domain.usecase.auth.SignOutUseCase
import com.example.fizyoapp.domain.usecase.auth.SignUpUseCase
import com.example.fizyoapp.domain.usecase.exercisesexamplesscreen.GetExerciseCategoriesUseCase
import com.example.fizyoapp.domain.usecase.exercisesexamplesscreen.PopulateDatabaseUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.AddPainRecordUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.DeletePainRecordUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.GetLatestPainRecordUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.GetPainRecordsUseCase
import com.example.fizyoapp.domain.usecase.mainscreen.UpdatePainRecordUseCase
import com.example.fizyoapp.domain.usecase.messagesscreen.GetChatThreadsUseCase
import com.example.fizyoapp.domain.usecase.messagesscreen.GetMessagesUseCase
import com.example.fizyoapp.domain.usecase.messagesscreen.MarkMessagesAsReadUseCase
import com.example.fizyoapp.domain.usecase.messagesscreen.SendMessageUseCase
import com.example.fizyoapp.domain.usecase.note.AddUpdateToNoteUseCase
import com.example.fizyoapp.domain.usecase.note.CreateNoteUseCase
import com.example.fizyoapp.domain.usecase.note.DeleteNoteUseCase
import com.example.fizyoapp.domain.usecase.note.GetNoteByIdUseCase
import com.example.fizyoapp.domain.usecase.note.GetNotesByPhysiotherapistIdUseCase
import com.example.fizyoapp.domain.usecase.note.UpdateNoteUpdateUseCase
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
import com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen.MessagesScreenViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.ExercisesExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.CoreExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.hip.HipExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.leg.LegExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.lowerback.LowerBackExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.neck.NeckExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.shoulder.ShoulderExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.usermainscreen.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
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
    fun providePainRepository(firestore: FirebaseFirestore): PainTrackingRepository {
        return PainTrackingRepositoryImpl(firestore)
    }


    @Provides
    @Singleton
    fun provideGetLatestPainRecordUseCase(painRepository: PainTrackingRepository): GetLatestPainRecordUseCase {
        return GetLatestPainRecordUseCase(painRepository)
    }

    @Provides
    @Singleton
    fun provideAddPainRecordUseCase(painRepository: PainTrackingRepository): AddPainRecordUseCase {
        return AddPainRecordUseCase(painRepository)
    }

    @Provides
    @Singleton
    fun provideGetPainRecordsForUserUseCase(painRepository: PainTrackingRepository): GetPainRecordsUseCase {
        return GetPainRecordsUseCase (painRepository)
    }



    // Main Screen ViewModels
    @Provides
    @Singleton
    fun provideUserViewModel(
        getCurrentUseCase: GetCurrentUseCase,
        signOutUseCase: SignOutUseCase,
        getUserProfileUseCase: GetUserProfileUseCase,
        getLatestPainRecordUseCase: GetLatestPainRecordUseCase,
    ): UserViewModel {
        return UserViewModel(
            getCurrentUseCase,
            signOutUseCase,
            getUserProfileUseCase,
            getLatestPainRecordUseCase)
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

    @Provides
    @Singleton
    fun providesExexrciseExamplesVideoDatabase(@ApplicationContext context: Context): VideoDatabase {
        return VideoDatabase.getDatabase(context)
    }

    @Provides
    @Singleton
    fun providesExercisesExamplesVideoDao(videoDatabase: VideoDatabase): VideoDao {
        return videoDatabase.videoDao()
    }

    @Provides
    @Singleton
    fun providevideoRepository(videoDao: VideoDao): ExamplesOfExerciseRepository {
        return ExamplesOfExercisesRepositoryImp(videoDao)
    }

    @Provides
    @Singleton
    fun provideShoulderExercisesOfExamplesViewModel(
        repository: ExamplesOfExerciseRepository,
        @ApplicationContext context: Context
    ): ShoulderExercisesOfExamplesViewModel {
        return ShoulderExercisesOfExamplesViewModel(repository, context)
    }

    @Provides
    @Singleton
    fun provideNeckExercisesOfExamplesViewModel(
        repository: ExamplesOfExerciseRepository,
        @ApplicationContext context: Context
    ): NeckExercisesOfExamplesViewModel {
        return NeckExercisesOfExamplesViewModel(repository, context)
    }

    @Provides
    @Singleton
    fun provideCoreExercisesOfExamplesViewModel(
        repository: ExamplesOfExerciseRepository,
        @ApplicationContext context: Context
    ): CoreExercisesOfExamplesViewModel {
        return CoreExercisesOfExamplesViewModel(repository, context)
    }

    @Provides
    @Singleton
    fun provideLowerBackExercisesOfExamplesViewModel(
        repository: ExamplesOfExerciseRepository,
        @ApplicationContext context: Context
    ): LowerBackExercisesOfExamplesViewModel {
        return LowerBackExercisesOfExamplesViewModel(repository, context)
    }

    @Provides
    @Singleton
    fun provideLegExercisesOfExamplesViewModel(
        repository: ExamplesOfExerciseRepository,
        @ApplicationContext context: Context
    ): LegExercisesOfExamplesViewModel {
        return LegExercisesOfExamplesViewModel(repository, context)
    }

    @Provides
    @Singleton
    fun provideHipExercisesOfExamplesViewModel(
        repository: ExamplesOfExerciseRepository,
        @ApplicationContext context: Context
    ): HipExercisesOfExamplesViewModel {
        return HipExercisesOfExamplesViewModel(repository, context)
    }

    @Provides
    @Singleton
    fun provideExercisesDatabase(
        @ApplicationContext context: Context
    ): ExercisesDatabase {
        return ExercisesDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideExerciseCategoryDao(
        database: ExercisesDatabase
    ): OrnekEgzersizlerGirisDao {
        return database.exerciseCategoryDao()
    }

    @Provides
    @Singleton
    fun provideExercisesExamplesRepository(
        dao: OrnekEgzersizlerGirisDao
    ): ExercisesExamplesRepository {
        return ExercisesExamplesRepositoryImpl(dao)
    }

    @Provides
    @Singleton
    fun provideGetExerciseCategoriesUseCase(
        repository: ExercisesExamplesRepository
    ): GetExerciseCategoriesUseCase {
        return GetExerciseCategoriesUseCase(repository)
    }

    @Provides
    @Singleton
    fun providePopulateDatabaseUseCase(
        repository: ExercisesExamplesRepository
    ): PopulateDatabaseUseCase {
        return PopulateDatabaseUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideExercisesExamplesViewModel(
        getExerciseCategoriesUseCase: GetExerciseCategoriesUseCase,
        populateDatabaseUseCase: PopulateDatabaseUseCase
    ): ExercisesExamplesViewModel {
        return ExercisesExamplesViewModel(getExerciseCategoriesUseCase, populateDatabaseUseCase)
    }

    // Note Repository ve Use-Case'ler
    @Provides
    @Singleton
    fun provideNoteRepository(): NoteRepository {
        return NoteRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideGetNotesByPhysiotherapistIdUseCase(repository: NoteRepository): GetNotesByPhysiotherapistIdUseCase {
        return GetNotesByPhysiotherapistIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetNoteByIdUseCase(repository: NoteRepository): GetNoteByIdUseCase {
        return GetNoteByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreateNoteUseCase(repository: NoteRepository): CreateNoteUseCase {
        return CreateNoteUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddUpdateToNoteUseCase(repository: NoteRepository): AddUpdateToNoteUseCase {
        return AddUpdateToNoteUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteNoteUseCase(repository: NoteRepository): DeleteNoteUseCase {
        return DeleteNoteUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateNoteUpdateUseCase(repository: NoteRepository): UpdateNoteUpdateUseCase {
        return UpdateNoteUpdateUseCase(repository)
    }

    // Message Repository ve Use-Case'ler (master'dan gelen)
    @Provides
    @Singleton
    fun provideMessagesScreenViewModel(
        getChatThreadsUseCase: GetChatThreadsUseCase
    ): MessagesScreenViewModel {
        return MessagesScreenViewModel(getChatThreadsUseCase)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        authRepository: AuthRepository,
        userProfileRepository: UserProfileRepository,
        physiotherapistProfileRepository: PhysiotherapistProfileRepository
    ): MessageRepository {
        return MessageRepositoryImpl(
            userProfileRepository, authRepository, physiotherapistProfileRepository
        )
    }

    @Provides
    @Singleton
    fun provideGetChatThreadsUseCase(
        messageRepository: MessageRepository,
        authRepository: AuthRepository
    ): GetChatThreadsUseCase {
        return GetChatThreadsUseCase(messageRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideGetMessagesUseCase(
        messageRepository: MessageRepository,
        authRepository: AuthRepository
    ): GetMessagesUseCase {
        return GetMessagesUseCase(messageRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideSendMessageUseCase(
        messageRepository: MessageRepository,
        authRepository: AuthRepository
    ): SendMessageUseCase {
        return SendMessageUseCase(messageRepository, authRepository)
    }

    @Provides
    @Singleton
    fun provideMarkMessagesAsReadUseCase(messageRepository: MessageRepository, authRepository: AuthRepository
    ): MarkMessagesAsReadUseCase {
        return MarkMessagesAsReadUseCase(messageRepository, authRepository)
    }


    @Provides
    fun provideUpdatePainRecordUseCase(repository: PainTrackingRepository): UpdatePainRecordUseCase {
        return UpdatePainRecordUseCase(repository)
    }

    @Provides
    fun provideDeletePainRecordUseCase(repository: PainTrackingRepository): DeletePainRecordUseCase {
        return DeletePainRecordUseCase(repository)
    }
}