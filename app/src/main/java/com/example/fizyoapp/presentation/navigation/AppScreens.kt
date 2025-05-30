package com.example.fizyoapp.presentation.navigation

sealed class AppScreens(val route: String) {
    object SplashScreen: AppScreens("splash_screen")
    object LoginScreen: AppScreens("login_screen")
    object RegisterScreen: AppScreens("register_screen")
    object MediaViewerScreen :AppScreens("mediaviewer_screen")

    object PhysiotherapistMainScreen: AppScreens("physiotherapist_main_screen")
    object UserMainScreen: AppScreens("user_main_screen")
    object UserProfileSetupScreen: AppScreens("user_profile_setup")
    object UserInformationScreen: AppScreens("user_information_screen")
    object PhysiotherapistProfileSetupScreen: AppScreens("physiotherapist_profile_setup")
    object PhysiotherapistProfileUpdateScreen: AppScreens("physiotherapist_profile_update")
    object PhysiotherapistDetailScreen: AppScreens("physiotherapist_detail_screen")
    object ExerciseManagementScreen :AppScreens("exercise_management_screen")
    object AddExerciseScreen : AppScreens("add_exercise_screen")
    object ExerciseCategoriesScreen : AppScreens("category_exercises_screen/{categoryId}")
    object CreateExercisePlanScreen : AppScreens("create_exercise_plan_screen")
    object ExercisePlanDetailScreen : AppScreens("exercise_plan_detail_screen/{planId}")
    object OrnekEgzersizler: AppScreens("ornek_egzersizler")
    object ExercisesScreen: AppScreens("exercises_screen")
    object ShoulderExercisesScreen: AppScreens("shoulder_egzersizleri")
    object NeckExercisesScreen: AppScreens("neck_exercises")
    object LowerBackExercisesScreen: AppScreens("lowerback_exercises")
    object LegExercisesScreen: AppScreens("leg_exercises")
    object CoreExercisesScreen: AppScreens("core_exercises")
    object HipExercisesScreen: AppScreens("hip_exercises")
    object UserExercisePlansScreen: AppScreens("user_exercise_plan")
    object NotesScreen: AppScreens("notes_screen")
    object NoteDetailScreen: AppScreens("note_detail_screen/{noteId}") {
        fun createRoute(noteId: String): String {
            return "note_detail_screen/$noteId"
        }
    }
    object EditExerciseScreen : AppScreens("edit_exercise_screen/{exerciseId}")
    object AddNoteScreen: AppScreens("add_note_screen")
    object MessagesScreen: AppScreens("messages_screen")
    object MessagesDetailScreen: AppScreens("message_detail_screen/{userId}") {
        fun createMessageDetailRoute(userId: String): String {
            return "message_detail_screen/$userId"
        }
    }
    object SearchScreen: AppScreens("search_screen")
    object PainTrackingScreen: AppScreens("pain_tracking")
    object AddPainRecordScreen: AppScreens("add_pain_record")
    object RehabilitationHistoryScreen: AppScreens("rehabilitation_history_screen")
    object MedicalRecordsScreen: AppScreens("medical_records_screen")
    object MedicalRecordDetailScreen: AppScreens("medical_record_detail_screen/{recordId}") {
        fun createRoute(recordId: String): String {
            return "medical_record_detail_screen/$recordId"
        }
    }
    object UploadMedicalRecordScreen: AppScreens("upload_medical_record_screen")
    object EvaluationFormsScreen: AppScreens("evaluation_forms_screen")
    object EvaluationFormDetailScreen: AppScreens("evaluation_form_detail_screen/{formId}") {
        fun createRoute(formId: String): String {
            return "evaluation_form_detail_screen/$formId"
        }
    }
    object EvaluationFormCreateScreen: AppScreens("evaluation_form_create_screen/{formType}") {
        fun createRoute(formType: String): String {
            return "evaluation_form_create_screen/$formType"
        }
    }
    object RadyolojikGoruntulerScreen: AppScreens("radiology_images_screen")
    object RadiologyImagesScreen: AppScreens("radiology_images_screen")
    object RadiologyImageDetailScreen: AppScreens("radiology_image_detail_screen/{imageId}") {
        fun createRoute(imageId: String): String {
            return "radiology_image_detail_screen/$imageId"
        }
    }
    object MedicalReportScreen : AppScreens("medical_report_screen")
    object FormResponseDetailScreen : AppScreens("form_response_detail_screen")
    object RadyolojikGoruntuEkleScreen: AppScreens("radyolojik_goruntu_ekle_screen")
    object HastaliklarimScreen: AppScreens("hastaliklarim_screen")
    object SocialMediaScreen: AppScreens("social_media_screen")
    object SocialMediaSearchScreen: AppScreens("social_media_search_screen")
    object CreatePostScreen: AppScreens("create_post_screen")
    object PhysiotherapistSocialProfile: AppScreens("physiotherapist_social_profile") {
        fun createRoute(physiotherapistId: String): String {
            return "physiotherapist_social_profile/$physiotherapistId"
        }
    }
    object NotificationScreen: AppScreens("notification_screen")
    object PostDetailScreen: AppScreens("post_detail_screen/{postId}") {
        fun createRoute(postId: String): String {
            return "post_detail_screen/$postId"
        }
    }
    object EditPostScreen: AppScreens("edit_post_screen/{postId}") {
        fun createRoute(postId: String): String {
            return "edit_post_screen/$postId"
        }
    }
    object EditExercisePlanScreen : AppScreens("edit_exercise_plan_screen/{planId}")
    object AppointmentBookingScreen: AppScreens("appointment_booking_screen/{physiotherapistId}") {
        fun createRoute(physiotherapistId: String): String {
            return "appointment_booking_screen/$physiotherapistId"
        }
    }
    object PhysiotherapistCalendarScreen: AppScreens("physiotherapist_calendar_screen")
    object ForgotPasswordScreen: AppScreens("forgot_password_screen")

    object CreateAdvertisementScreen: AppScreens("create_advertisement_screen")
    object AdvertisementPaymentScreen: AppScreens("advertisement_payment_screen")
    object AdvertisementSuccessScreen: AppScreens("advertisement_success_screen")
    object AdvertisementDetailScreen: AppScreens("advertisement_detail_screen/{advertisementId}") {
        fun createRoute(advertisementId: String): String {
            return "advertisement_detail_screen/$advertisementId"
        }
    }
}