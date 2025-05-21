package com.example.fizyoapp.presentation.navigation

sealed class AppScreens(val route:String) {
    object SplashScreen: AppScreens("splash_screen")
    object OrnekEgzersizler: AppScreens("ornek_egzersizler")
    object SearchScreen: AppScreens("search_screen")
    object ProfilScreen: AppScreens("profil_screen")
    object PaylasimlarScreen: AppScreens("paylasimlar_screen")
    object HastaliklarimScreen: AppScreens("hastaliklarim_screen")
    object RadyolojikGoruntuEkleScreen: AppScreens("radyolojik_goruntu_ekle_screen")
    object LoginScreen: AppScreens("login_screen")
    object RegisterScreen: AppScreens("register_screen")
    object PhysiotherapistMainScreen: AppScreens("physiotherapist_main_screen")
    object UserMainScreen: AppScreens("user_main_screen")
    object UserProfileSetupScreen: AppScreens("user_profile_setup")
    object UserInformationScreen: AppScreens("user_information_screen")
    object PhysiotherapistProfileSetupScreen: AppScreens("physiotherapist_profile_setup")
    object PhysiotherapistProfileUpdateScreen: AppScreens("physiotherapist_profile_update")
    object PhysiotherapistDetailScreen: AppScreens("physiotherapist_detail_screen")
    object ShoulderExercisesScreen:AppScreens("shoulder_egzersizleri")
    object NeckExercisesScreen:AppScreens("neck_exercises")
    object LowerBackExercisesScreen:AppScreens("lowerback_exercises")
    object LegExercisesScreen:AppScreens("leg_exercises")
    object CoreExercisesScreen:AppScreens("core_exercises")
    object HipExercisesScreen:AppScreens("hip_exercises")

    // Not ekranları
    object NotesScreen: AppScreens("notes_screen")
    object NoteDetailScreen: AppScreens("note_detail_screen/{noteId}")
    object AddNoteScreen: AppScreens("add_note_screen")

    // Mesaj ekranları (master'dan)
    object MessagesScreen : AppScreens("messages_screen")
    object MessagesDetailScreen : AppScreens("message_detail_screen/{userId}") {
        fun createMessageDetailRoute(userId: String): String {
            return "message_detail_screen/$userId"
        }
    }

    // Ayarlar ekranı (master'dan)
    object SettingsScreen : AppScreens("settings")
    object PainTrackingScreen : AppScreens("pain_tracking")
    object AddPainRecordScreen : AppScreens("add_pain_record")
    object ProfileScreen : AppScreens("profile")
    object ExercisesScreen : AppScreens("exercises_screen")
    object RehabilitationHistoryScreen : AppScreens("rehabilitation_history_screen")
    object MedicalRecordsScreen: AppScreens("medical_records_screen")
    object MedicalRecordDetailScreen: AppScreens("medical_record_detail_screen/{recordId}") {
        fun createRoute(recordId: String): String {
            return "medical_record_detail_screen/$recordId"
        }
    }
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
    object RadiologyImageDetailScreen: AppScreens("radiology_image_detail_screen/{imageId}") {
        fun createRoute(imageId: String): String {
            return "radiology_image_detail_screen/$imageId"
        }
    }
    object MedicalReportScreen : AppScreens("medical_report_screen")

}