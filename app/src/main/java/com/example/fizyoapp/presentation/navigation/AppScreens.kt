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

    object SocialFeedScreen : AppScreens("social_feed")
    object SocialProfileScreen : AppScreens("social_profile/{userId}") {
        fun createRoute(userId: String): String = "social_profile/$userId"
    }
    object CreatePostScreen : AppScreens("create_post")
    object CommentsScreen : AppScreens("comments/{postId}") {
        fun createRoute(postId: String): String = "comments/$postId"
    }
}