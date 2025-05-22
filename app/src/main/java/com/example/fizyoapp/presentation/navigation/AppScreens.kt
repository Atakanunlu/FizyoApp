package com.example.fizyoapp.presentation.navigation

sealed class AppScreens(val route: String) {

    object SplashScreen: AppScreens("splash_screen")
    object LoginScreen: AppScreens("login_screen")
    object RegisterScreen: AppScreens("register_screen")


    object PhysiotherapistMainScreen: AppScreens("physiotherapist_main_screen")
    object UserMainScreen: AppScreens("user_main_screen")


    object UserProfileSetupScreen: AppScreens("user_profile_setup")
    object UserInformationScreen: AppScreens("user_information_screen")
    object PhysiotherapistProfileSetupScreen: AppScreens("physiotherapist_profile_setup")
    object PhysiotherapistProfileUpdateScreen: AppScreens("physiotherapist_profile_update")
    object PhysiotherapistDetailScreen: AppScreens("physiotherapist_detail_screen")
    object ProfilScreen: AppScreens("profil_screen")



    object OrnekEgzersizler: AppScreens("ornek_egzersizler")
    object ExercisesScreen: AppScreens("exercises_screen")
    object ShoulderExercisesScreen: AppScreens("shoulder_egzersizleri")
    object NeckExercisesScreen: AppScreens("neck_exercises")
    object LowerBackExercisesScreen: AppScreens("lowerback_exercises")
    object LegExercisesScreen: AppScreens("leg_exercises")
    object CoreExercisesScreen: AppScreens("core_exercises")
    object HipExercisesScreen: AppScreens("hip_exercises")

    object NotesScreen: AppScreens("notes_screen")
    object NoteDetailScreen: AppScreens("note_detail_screen/{noteId}") {
        fun createRoute(noteId: String): String {
            return "note_detail_screen/$noteId"
        }
    }
    object AddNoteScreen: AppScreens("add_note_screen")

    object MessagesScreen: AppScreens("messages_screen")
    object MessagesDetailScreen: AppScreens("message_detail_screen/{userId}") {
        fun createMessageDetailRoute(userId: String): String {
            return "message_detail_screen/$userId"
        }
    }

    object SettingsScreen: AppScreens("settings")


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


    object RadiologyImagesScreen: AppScreens("radiology_images_screen")
    object RadiologyImageDetailScreen: AppScreens("radiology_image_detail_screen/{imageId}") {
        fun createRoute(imageId: String): String {
            return "radiology_image_detail_screen/$imageId"
        }
    }
    object RadyolojikGoruntuEkleScreen: AppScreens("radyolojik_goruntu_ekle_screen")


    object SearchScreen: AppScreens("search_screen")
    object PaylasimlarScreen: AppScreens("paylasimlar_screen")
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
}