package com.example.fizyoapp.di

import android.content.ContentResolver
import android.content.Context
import com.example.fizyoapp.data.local.dao.exerciseexamplesscreen.OrnekEgzersizlerGirisDao
import com.example.fizyoapp.data.local.dao.exercisevideos.VideoDao
import com.example.fizyoapp.data.local.database.exerciseexamplesscreen.ExercisesDatabase
import com.example.fizyoapp.data.local.database.exercisevideos.VideoDatabase
import com.example.fizyoapp.data.repository.ExercisesExamplesRepositoryImpl
import com.example.fizyoapp.data.repository.appointment.AppointmentRepository
import com.example.fizyoapp.data.repository.appointment.AppointmentRepositoryImpl
import com.example.fizyoapp.data.repository.auth.AuthRepository
import com.example.fizyoapp.data.repository.auth.AuthRepositoryImpl
import com.example.fizyoapp.data.repository.exercisesexamplesscreen.ExercisesExamplesRepository
import com.example.fizyoapp.data.repository.exercisevideos.ExamplesOfExerciseRepository
import com.example.fizyoapp.data.repository.exercisevideos.ExamplesOfExercisesRepositoryImp
import com.example.fizyoapp.data.repository.illnessrecordscreen.evaluationformscreen.EvaluationFormRepository
import com.example.fizyoapp.data.repository.illnessrecordscreen.evaluationformscreen.EvaluationFormRepositoryImpl
import com.example.fizyoapp.data.repository.illnessrecordscreen.medicalrecord.MedicalReportRepository
import com.example.fizyoapp.data.repository.illnessrecordscreen.medicalreport.MedicalReportRepositoryImpl
import com.example.fizyoapp.data.repository.illnessrecordscreen.radiologicalimagesscreen.RadyolojikGoruntuRepository
import com.example.fizyoapp.data.repository.illnessrecordscreen.radiologicalimagesscreen.RadyolojikGoruntuRepositoryImpl
import com.example.fizyoapp.data.repository.follow.FollowRepository
import com.example.fizyoapp.data.repository.follow.FollowRepositoryImpl
import com.example.fizyoapp.data.repository.mainscreen.painrecord.PainTrackingRepository
import com.example.fizyoapp.data.repository.mainscreen.PainTrackingRepositoryImpl
import com.example.fizyoapp.data.repository.messagesscreen.MessageRepositoryImpl
import com.example.fizyoapp.data.repository.messagesscreen.MessagesRepository
import com.example.fizyoapp.data.repository.note.NoteRepository
import com.example.fizyoapp.data.repository.note.NoteRepositoryImpl
import com.example.fizyoapp.data.repository.notification.NotificationRepository
import com.example.fizyoapp.data.repository.notification.NotificationRepositoryImpl
import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepository
import com.example.fizyoapp.data.repository.physiotherapist_profile.PhysiotherapistProfileRepositoryImpl
import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepository
import com.example.fizyoapp.data.repository.socialmedia.SocialMediaRepositoryImpl
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepository
import com.example.fizyoapp.data.repository.user_profile.UserProfileRepositoryImpl
import com.example.fizyoapp.domain.usecase.appointment.BlockTimeSlotUseCase
import com.example.fizyoapp.domain.usecase.appointment.CreateAppointmentUseCase
import com.example.fizyoapp.domain.usecase.appointment.GetAvailableTimeSlotsUseCase
import com.example.fizyoapp.domain.usecase.appointment.GetPhysiotherapistAppointmentsUseCase
import com.example.fizyoapp.domain.usecase.appointment.GetUserAppointmentsUseCase
import com.example.fizyoapp.domain.usecase.appointment.UnblockTimeSlotUseCase
import com.example.fizyoapp.domain.usecase.appointment.UpdateAppointmentNotesUseCase
import com.example.fizyoapp.domain.usecase.auth.CheckEmailVerifiedUseCase
import com.example.fizyoapp.domain.usecase.auth.GetCurrentPhysiotherapistUseCase
import com.example.fizyoapp.domain.usecase.auth.GetCurrentUseCase
import com.example.fizyoapp.domain.usecase.auth.GetUserRoleUseCase
import com.example.fizyoapp.domain.usecase.auth.ResetPasswordUseCase
import com.example.fizyoapp.domain.usecase.auth.SendEmailVerificationUseCase
import com.example.fizyoapp.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.example.fizyoapp.domain.usecase.auth.SignInUseCase
import com.example.fizyoapp.domain.usecase.auth.SignOutUseCase
import com.example.fizyoapp.domain.usecase.auth.SignUpUseCase
import com.example.fizyoapp.domain.usecase.auth.VerifyPasswordResetCodeUseCase
import com.example.fizyoapp.domain.usecase.exercisesexamplesscreen.GetExerciseCategoriesUseCase
import com.example.fizyoapp.domain.usecase.exercisesexamplesscreen.PopulateDatabaseUseCase
import com.example.fizyoapp.domain.usecase.follow.FollowPhysiotherapistUseCase
import com.example.fizyoapp.domain.usecase.follow.GetFollowersCountUseCase
import com.example.fizyoapp.domain.usecase.follow.GetFollowersUseCase
import com.example.fizyoapp.domain.usecase.follow.GetFollowingCountUseCase
import com.example.fizyoapp.domain.usecase.follow.GetFollowingUseCase
import com.example.fizyoapp.domain.usecase.follow.IsFollowingUseCase
import com.example.fizyoapp.domain.usecase.follow.UnfollowPhysiotherapistUseCase
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
import com.example.fizyoapp.domain.usecase.notification.CreateNotificationUseCase
import com.example.fizyoapp.domain.usecase.notification.DeleteNotificationUseCase
import com.example.fizyoapp.domain.usecase.notification.GetNotificationsUseCase
import com.example.fizyoapp.domain.usecase.notification.GetUnreadNotificationsCountUseCase
import com.example.fizyoapp.domain.usecase.notification.MarkAllNotificationsAsReadUseCase
import com.example.fizyoapp.domain.usecase.notification.MarkNotificationAsReadUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.CheckPhysiotherapistProfileCompletedUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetAllPhysiotherapistsUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistByIdUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.GetPhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.UpdatePhysiotherapistProfileUseCase
import com.example.fizyoapp.domain.usecase.physiotherapist_profile.UploadPhysiotherapistProfilePhotoUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.AddCommentUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.CreatePostUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.DeleteCommentUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.DeletePostUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.GetAllPostsUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.GetCommentsByPostIdUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.GetPostByIdUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.LikePostUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.UnlikePostUseCase
import com.example.fizyoapp.domain.usecase.socialmedia.UpdatePostUseCase
import com.example.fizyoapp.domain.usecase.user_profile.CheckProfileCompletedUseCase
import com.example.fizyoapp.domain.usecase.user_profile.GetUserProfileUseCase
import com.example.fizyoapp.domain.usecase.user_profile.UpdateUserProfileUseCase
import com.example.fizyoapp.domain.usecase.user_profile.UploadProfilePhotoUseCase
import com.example.fizyoapp.presentation.appointment.calendar.CalenderUserDetailsViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.ExercisesExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.CoreExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.hip.HipExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.leg.LegExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.lowerback.LowerBackExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.neck.NeckExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.shoulder.ShoulderExercisesOfExamplesViewModel
import com.example.fizyoapp.presentation.user.rehabilitation.RehabilitationHistoryViewModel
import com.example.fizyoapp.presentation.user.usermainscreen.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
    fun provideMessagesRepository(   userProfileRepository: UserProfileRepository,
                                     authRepository: AuthRepository,
                                     physiotherapistProfileRepository: PhysiotherapistProfileRepository): MessagesRepository {
        return MessageRepositoryImpl(userProfileRepository,authRepository,physiotherapistProfileRepository)
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
    @Provides
    @Singleton
    fun provideGetChatThreadsUseCase(messagesRepository: MessagesRepository,authRepository: AuthRepository): GetChatThreadsUseCase {
        return GetChatThreadsUseCase(messagesRepository,authRepository)
    }

    @Provides
    @Singleton
    fun provideGetMessagesUseCase(messagesRepository: MessagesRepository,authRepository: AuthRepository): GetMessagesUseCase {
        return GetMessagesUseCase(messagesRepository,authRepository)
    }

    @Provides
    @Singleton
    fun provideSendMessageUseCase(messagesRepository: MessagesRepository,authRepository: AuthRepository): SendMessageUseCase {
        return SendMessageUseCase(messagesRepository,authRepository)
    }

    @Provides
    @Singleton
    fun provideMarkMessagesAsReadUseCase(messagesRepository: MessagesRepository,authRepository: AuthRepository): MarkMessagesAsReadUseCase {
        return MarkMessagesAsReadUseCase(messagesRepository,authRepository)
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


    @Provides
    fun provideUpdatePainRecordUseCase(repository: PainTrackingRepository): UpdatePainRecordUseCase {
        return UpdatePainRecordUseCase(repository)
    }

    @Provides
    fun provideDeletePainRecordUseCase(repository: PainTrackingRepository): DeletePainRecordUseCase {
        return DeletePainRecordUseCase(repository)
    }
    @Provides
    @Singleton
    fun provideRadyolojikGoruntuRepository(

        storage: FirebaseStorage
    ): RadyolojikGoruntuRepository {
        return RadyolojikGoruntuRepositoryImpl(storage)
    }
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        // Özel yapılandırma
        val storage = FirebaseStorage.getInstance()
        // İsteğe bağlı diğer yapılandırmalar
        return storage
    }
    @Provides
    @Singleton
    fun provideMedicalReportRepository(
        storage: FirebaseStorage
    ): MedicalReportRepository {
        return MedicalReportRepositoryImpl(storage)
    }

    @Provides
    @Singleton
    fun provideEvaluationFormRepository(
        firestore: FirebaseFirestore
    ): EvaluationFormRepository {
        return EvaluationFormRepositoryImpl(firestore)
    }


    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideSocialMediaRepository(contentResolver: ContentResolver): SocialMediaRepository {
        return SocialMediaRepositoryImpl(contentResolver)
    }

    @Provides
    @Singleton
    fun provideGetAllPostsUseCase(repository: SocialMediaRepository): GetAllPostsUseCase {
        return GetAllPostsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreatePostUseCase(repository: SocialMediaRepository): CreatePostUseCase {
        return CreatePostUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetPostByIdUseCase(repository: SocialMediaRepository): GetPostByIdUseCase {
        return GetPostByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideLikePostUseCase(repository: SocialMediaRepository): LikePostUseCase {
        return LikePostUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUnlikePostUseCase(repository: SocialMediaRepository): UnlikePostUseCase {
        return UnlikePostUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCommentsByPostIdUseCase(repository: SocialMediaRepository): GetCommentsByPostIdUseCase {
        return GetCommentsByPostIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddCommentUseCase(repository: SocialMediaRepository): AddCommentUseCase {
        return AddCommentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeletePostUseCase(repository: SocialMediaRepository): DeletePostUseCase {
        return DeletePostUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteCommentUseCase(repository: SocialMediaRepository): DeleteCommentUseCase {
        return DeleteCommentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdatePostUseCase(repository: SocialMediaRepository): UpdatePostUseCase {
        return UpdatePostUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideFollowRepository(firestore: FirebaseFirestore): FollowRepository {
        return FollowRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideFollowPhysiotherapistUseCase(repository: FollowRepository): FollowPhysiotherapistUseCase {
        return FollowPhysiotherapistUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUnfollowPhysiotherapistUseCase(repository: FollowRepository): UnfollowPhysiotherapistUseCase {
        return UnfollowPhysiotherapistUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideIsFollowingUseCase(repository: FollowRepository): IsFollowingUseCase {
        return IsFollowingUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetFollowersCountUseCase(repository: FollowRepository): GetFollowersCountUseCase {
        return GetFollowersCountUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetFollowingCountUseCase(repository: FollowRepository): GetFollowingCountUseCase {
        return GetFollowingCountUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetFollowersUseCase(repository: FollowRepository): GetFollowersUseCase {
        return GetFollowersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetFollowingUseCase(repository: FollowRepository): GetFollowingUseCase {
        return GetFollowingUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(firestore: FirebaseFirestore): NotificationRepository {
        return NotificationRepositoryImpl(firestore)
    }

    @Provides
    @Singleton
    fun provideGetNotificationsUseCase(repository: NotificationRepository): GetNotificationsUseCase {
        return GetNotificationsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetUnreadNotificationsCountUseCase(repository: NotificationRepository): GetUnreadNotificationsCountUseCase {
        return GetUnreadNotificationsCountUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkNotificationAsReadUseCase(repository: NotificationRepository): MarkNotificationAsReadUseCase {
        return MarkNotificationAsReadUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideMarkAllNotificationsAsReadUseCase(repository: NotificationRepository): MarkAllNotificationsAsReadUseCase {
        return MarkAllNotificationsAsReadUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreateNotificationUseCase(repository: NotificationRepository): CreateNotificationUseCase {
        return CreateNotificationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteNotificationUseCase(repository: NotificationRepository): DeleteNotificationUseCase {
        return DeleteNotificationUseCase(repository)
    }
    @Provides
    @Singleton
    fun provideAppointmentRepository(): AppointmentRepository {
        return AppointmentRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideGetAvailableTimeSlotsUseCase(repository: AppointmentRepository): GetAvailableTimeSlotsUseCase {
        return GetAvailableTimeSlotsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreateAppointmentUseCase(repository: AppointmentRepository): CreateAppointmentUseCase {
        return CreateAppointmentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetUserAppointmentsUseCase(repository: AppointmentRepository): GetUserAppointmentsUseCase {
        return GetUserAppointmentsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetPhysiotherapistAppointmentsUseCase(repository: AppointmentRepository): GetPhysiotherapistAppointmentsUseCase {
        return GetPhysiotherapistAppointmentsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideBlockTimeSlotUseCase(repository: AppointmentRepository): BlockTimeSlotUseCase {
        return BlockTimeSlotUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUnblockTimeSlotUseCase(repository: AppointmentRepository): UnblockTimeSlotUseCase {
        return UnblockTimeSlotUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCurrentPhysiotherapistUseCase(authRepository: AuthRepository): GetCurrentPhysiotherapistUseCase {
        return GetCurrentPhysiotherapistUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideCalenderUserDetailsViewModel(getUserProfileUseCase: GetUserProfileUseCase): CalenderUserDetailsViewModel {
        return CalenderUserDetailsViewModel(getUserProfileUseCase)
    }

    @Provides
    @Singleton
    fun provideUpdateAppointmentNotesUseCase(repository: AppointmentRepository): UpdateAppointmentNotesUseCase {
        return UpdateAppointmentNotesUseCase(repository)
    }


    @Provides
    @Singleton
    fun provideRehabilitationHistoryViewModel(
        getUserAppointmentsUseCase: GetUserAppointmentsUseCase,
        getPhysiotherapistByIdUseCase: GetPhysiotherapistByIdUseCase,
        getCurrentUserUseCase: GetCurrentUseCase
    ): RehabilitationHistoryViewModel {
        return RehabilitationHistoryViewModel(
            getUserAppointmentsUseCase,
            getPhysiotherapistByIdUseCase,
            getCurrentUserUseCase
        )
    }

    @Provides
    @Singleton
    fun provideSendEmailVerificationUseCase(authRepository: AuthRepository): SendEmailVerificationUseCase {
        return SendEmailVerificationUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideCheckEmailVerifiedUseCase(authRepository: AuthRepository): CheckEmailVerifiedUseCase {
        return CheckEmailVerifiedUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSendPasswordResetEmailUseCase(authRepository: AuthRepository): SendPasswordResetEmailUseCase {
        return SendPasswordResetEmailUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideVerifyPasswordResetCodeUseCase(authRepository: AuthRepository): VerifyPasswordResetCodeUseCase {
        return VerifyPasswordResetCodeUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideResetPasswordUseCase(authRepository: AuthRepository): ResetPasswordUseCase {
        return ResetPasswordUseCase(authRepository)
    }

}