package com.example.fizyoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen.MessagesDetailScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen.MessagesScreen
import com.example.fizyoapp.presentation.login.LoginScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_main_screen.PhysiotherapistMainScreen
import com.example.fizyoapp.presentation.register.RegisterScreen
import com.example.fizyoapp.presentation.user.usermainscreen.UserMainScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.paylasimlarscreen.PaylasimlarScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.profilscreen.ProfilScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.searchscreen.SearchScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.addnote.AddNoteScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail.NoteDetailScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notes.NotesEvent
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notes.NotesScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notes.NotesViewModel
import com.example.fizyoapp.presentation.physiotherapist.physiotherapistdetail.PhysiotherapistDetailScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_profile_screen.PhysiotherapistProfileSetupScreen
import com.example.fizyoapp.presentation.social.comments.CommentsScreen
import com.example.fizyoapp.presentation.social.create.CreatePostScreen
import com.example.fizyoapp.presentation.social.feed.SocialFeedScreen
import com.example.fizyoapp.presentation.social.profile.SocialProfileScreen
import com.example.fizyoapp.presentation.splashscreen.SplashScreen
import com.example.fizyoapp.presentation.user.hastaliklarim.HastaliklarimScreen
import com.example.fizyoapp.presentation.user.hastaliklarim.radyolojikgoruntuekle.RadyolojikGoruntuEkle
import com.example.fizyoapp.presentation.user.ornekegzersizler.OrnekEgzersizler
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.CoreExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.LegExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.LowerBackExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.NeckExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.ShoulderExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.hip.HipExercisesScreen
import com.example.fizyoapp.presentation.user.userprofile.UserProfileSetupScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AppScreens.SplashScreen.route
    ) {
        composable(AppScreens.SplashScreen.route) {
            SplashScreen(navController = navController)
        }
        composable(AppScreens.LoginScreen.route) {
            LoginScreen(navController = navController)
        }
        composable(AppScreens.RegisterScreen.route) {
            RegisterScreen(navController = navController)
        }
        composable(AppScreens.PhysiotherapistMainScreen.route) {
            PhysiotherapistMainScreen(navController = navController)
        }
        composable(AppScreens.UserMainScreen.route) {
            UserMainScreen(navController = navController)
        }
        composable(AppScreens.OrnekEgzersizler.route) {
            OrnekEgzersizler(navController = navController)
        }
        composable(AppScreens.SearchScreen.route) {
            SearchScreen(navController = navController)
        }
        composable(AppScreens.ProfilScreen.route) {
            ProfilScreen(navController = navController)
        }
        composable(AppScreens.PaylasimlarScreen.route) {
            PaylasimlarScreen(navController = navController)
        }
        composable(AppScreens.HastaliklarimScreen.route) {
            HastaliklarimScreen(navController = navController)
        }
        composable(AppScreens.RadyolojikGoruntuEkleScreen.route) {
            RadyolojikGoruntuEkle(navController = navController)
        }
        composable(AppScreens.ShoulderExercisesScreen.route){
            ShoulderExercisesScreen(navController=navController)
        }
        composable(AppScreens.LowerBackExercisesScreen.route){
            LowerBackExercisesScreen(navController=navController)
        }
        composable(AppScreens.NeckExercisesScreen.route){
            NeckExercisesScreen(navController=navController)
        }
        composable(AppScreens.HipExercisesScreen.route){
            HipExercisesScreen(navController=navController)
        }
        composable(AppScreens.LegExercisesScreen.route){
            LegExercisesScreen(navController=navController)
        }
        composable(AppScreens.CoreExercisesScreen.route){
            CoreExercisesScreen(navController=navController)
        }
        composable(AppScreens.UserProfileSetupScreen.route) {
            UserProfileSetupScreen(
                navController = navController,
                isFirstSetup = true
            )
        }



        composable(AppScreens.UserInformationScreen.route) {
            UserProfileSetupScreen(
                navController = navController,
                isFirstSetup = false
            )
        }
        composable(AppScreens.PhysiotherapistProfileSetupScreen.route) {
            PhysiotherapistProfileSetupScreen(
                navController = navController,
                isFirstSetup = true
            )
        }
        composable(AppScreens.PhysiotherapistProfileUpdateScreen.route) {
            PhysiotherapistProfileSetupScreen(
                navController = navController,
                isFirstSetup = false
            )
        }
        composable(
            route = "${AppScreens.PhysiotherapistDetailScreen.route}/{physiotherapistId}",
            arguments = listOf(navArgument("physiotherapistId") { type = NavType.StringType })
        ) {
            val physiotherapistId = it.arguments?.getString("physiotherapistId") ?: ""
            PhysiotherapistDetailScreen(navController = navController)
        }


        composable(AppScreens.NotesScreen.route) {
            NotesScreen(navController = navController)
        }
        composable(
            route = AppScreens.NoteDetailScreen.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            val noteId = it.arguments?.getString("noteId") ?: ""
            NoteDetailScreen(navController = navController, noteId = noteId)
        }
        composable(AppScreens.AddNoteScreen.route) {
            val notesViewModel = hiltViewModel<NotesViewModel>()
            AddNoteScreen(
                navController = navController,
                onBackWithRefresh = {
                    notesViewModel.onEvent(NotesEvent.Refresh)
                }
            )
        }


        composable(
            route = AppScreens.MessagesScreen.route
        ) {
            MessagesScreen(navController = navController)
        }
        composable(
            route = AppScreens.MessagesDetailScreen.route,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) {
            val userId = it.arguments?.getString("userId") ?: ""
            MessagesDetailScreen(
                navController = navController,
                userId = userId
            )
        }

        // Sosyal medya ekranları
        composable(
            route = AppScreens.SocialFeedScreen.route
        ) {
            SocialFeedScreen(navController = navController)
        }

        composable(
            route = AppScreens.SocialProfileScreen.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val userId = it.arguments?.getString("userId") ?: ""
            SocialProfileScreen(navController = navController)
        }

        composable(
            route = AppScreens.CreatePostScreen.route
        ) {
            CreatePostScreen(navController = navController)
        }

        composable(
            route = AppScreens.CommentsScreen.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) {
            val postId = it.arguments?.getString("postId") ?: ""
            CommentsScreen(navController = navController)
        }
    }

}
