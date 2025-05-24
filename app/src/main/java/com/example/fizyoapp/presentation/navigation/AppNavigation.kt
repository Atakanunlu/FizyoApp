package com.example.fizyoapp.presentation.navigation

import OrnekEgzersizler
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fizyoapp.presentation.advertisement.create.CreateAdvertisementScreen
import com.example.fizyoapp.presentation.advertisement.detail.AdvertisementDetailScreen
import com.example.fizyoapp.presentation.advertisement.payment.AdvertisementPaymentScreen
import com.example.fizyoapp.presentation.advertisement.success.AdvertisementSuccessScreen
import com.example.fizyoapp.presentation.appointment.booking.AppointmentBookingScreen
import com.example.fizyoapp.presentation.appointment.calendar.PhysiotherapistCalendarScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.messagesdetailscreen.MessagesDetailScreen
import com.example.fizyoapp.presentation.login.LoginScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_main_screen.PhysiotherapistMainScreen
import com.example.fizyoapp.presentation.register.RegisterScreen
import com.example.fizyoapp.presentation.user.usermainscreen.UserMainScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.searchscreen.SearchScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen.MessagesScreen
import com.example.fizyoapp.presentation.forgotpassword.ForgotPasswordScreen
import com.example.fizyoapp.presentation.physiotherapist.exercise.EditExerciseScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexercise.AddExerciseScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexerciseplan.CreateExercisePlanScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.exercisemanagement.ExerciseManagementScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexerciseplan.EditExercisePlanScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_profile_screen.PhysiotherapistProfileSetupScreen
import com.example.fizyoapp.presentation.splashscreen.SplashScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.CoreExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.hip.HipExercisesScreen
import com.example.fizyoapp.presentation.user.userprofile.UserProfileSetupScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.addnote.AddNoteScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail.NoteDetailScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notes.NotesEvent
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notes.NotesScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notes.NotesViewModel
import com.example.fizyoapp.presentation.physiotherapist.physiotherapistdetail.PhysiotherapistDetailScreen
import com.example.fizyoapp.presentation.user.illnessrecord.HastaliklarimScreen
import com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms.EvaluationFormDetailScreen
import com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms.EvaluationFormsScreen
import com.example.fizyoapp.presentation.user.illnessrecord.evaluationforms.FormResponseDetailScreen
import com.example.fizyoapp.presentation.user.illnessrecord.medicalrecord.MedicalReportScreen
import com.example.fizyoapp.presentation.user.illnessrecord.radyologicalimagesadd.RadyolojikGoruntulerScreen
import com.example.fizyoapp.presentation.socialmedia.createpost.CreatePostScreen
import com.example.fizyoapp.presentation.socialmedia.editpost.EditPostScreen
import com.example.fizyoapp.presentation.socialmedia.notification.NotificationScreen
import com.example.fizyoapp.presentation.user.egzersizlerim.ExercisePlanDetailScreen
import com.example.fizyoapp.presentation.user.egzersizlerim.UserExercisePlansScreen
import com.example.fizyoapp.presentation.socialmedia.physiotherapistsocialprofile.PhysiotherapistSocialProfileScreen
import com.example.fizyoapp.presentation.socialmedia.postdetail.PostDetailScreen
import com.example.fizyoapp.presentation.socialmedia.socialmediamain.SocialMediaScreen
import com.example.fizyoapp.presentation.socialmedia.socialmediasearch.SocialMediaSearchScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.leg.LegExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.lowerback.LowerBackExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.neck.NeckExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.shoulder.ShoulderExercisesScreen
import com.example.fizyoapp.presentation.user.rehabilitation.RehabilitationHistoryScreen
import com.example.fizyoapp.presentation.user.usermainscreen.PainTrackingScreen

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
        composable(AppScreens.HastaliklarimScreen.route){
            HastaliklarimScreen(navController=navController)
        }
        composable(AppScreens.UserProfileSetupScreen.route) {
            UserProfileSetupScreen(
                navController = navController,
                isFirstSetup = true
            )
        }
        composable(AppScreens.RadyolojikGoruntulerScreen.route){
            RadyolojikGoruntulerScreen(navController=navController)
        }
        composable(
            route = AppScreens.MedicalReportScreen.route
        ) {
            MedicalReportScreen(navController = navController)
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
            route=AppScreens.ExerciseManagementScreen.route
        ){
            ExerciseManagementScreen(navController=navController)
        }
        composable(
            route = AppScreens.MessagesScreen.route
        ) {
            MessagesScreen(navController = navController)
        }
        composable(
            route = AppScreens.MessagesDetailScreen.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MessagesDetailScreen(
                navController = navController,
                userId = userId
            )
        }
        composable(AppScreens.PainTrackingScreen.route) {
            PainTrackingScreen(navController = navController)
        }
        composable(
            route = AppScreens.EvaluationFormsScreen.route
        ) {
            EvaluationFormsScreen(navController = navController)
        }
        composable(
            route = AppScreens.EvaluationFormDetailScreen.route + "/{formId}",
            arguments = listOf(
                navArgument("formId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { entry ->
            val formId = entry.arguments?.getString("formId") ?: ""
            EvaluationFormDetailScreen(
                navController = navController,
                formId = formId
            )
        }
        composable(
            route = AppScreens.FormResponseDetailScreen.route + "/{responseId}",
            arguments = listOf(
                navArgument("responseId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { entry ->
            val responseId = entry.arguments?.getString("responseId") ?: ""
            FormResponseDetailScreen(
                navController = navController,
                responseId = responseId
            )
        }
        composable(AppScreens.SocialMediaScreen.route) {
            SocialMediaScreen(navController = navController)
        }
        composable(AppScreens.SocialMediaSearchScreen.route) {
            SocialMediaSearchScreen(navController = navController)
        }
        composable(AppScreens.CreatePostScreen.route) {
            CreatePostScreen(navController = navController)
        }
        composable(
            route = AppScreens.PostDetailScreen.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) {
            val postId = it.arguments?.getString("postId") ?: ""
            PostDetailScreen(navController = navController)
        }
        composable(
            route = AppScreens.EditPostScreen.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) {
            val postId = it.arguments?.getString("postId") ?: ""
            EditPostScreen(navController = navController)
        }
        composable(AppScreens.PhysiotherapistSocialProfile.route) {
            PhysiotherapistSocialProfileScreen(navController = navController)
        }
        composable(
            route = "${AppScreens.PhysiotherapistSocialProfile.route}/{physiotherapistId}",
            arguments = listOf(navArgument("physiotherapistId") {
                type = NavType.StringType
                nullable = true
            })
        ) {
            val physiotherapistId = it.arguments?.getString("physiotherapistId")
            PhysiotherapistSocialProfileScreen(
                navController = navController,
                physiotherapistId = physiotherapistId
            )
        }
        composable(AppScreens.NotificationScreen.route) {
            NotificationScreen(navController = navController)
        }
        composable(
            route = AppScreens.AppointmentBookingScreen.route,
            arguments = listOf(navArgument("physiotherapistId") { type = NavType.StringType })
        ) {
            val physiotherapistId = it.arguments?.getString("physiotherapistId") ?: ""
            AppointmentBookingScreen(navController = navController)
        }
        composable(AppScreens.PhysiotherapistCalendarScreen.route) {
            PhysiotherapistCalendarScreen(navController = navController)
        }
        composable(AppScreens.RehabilitationHistoryScreen.route) {
            RehabilitationHistoryScreen(navController = navController)
        }
        composable(AppScreens.ForgotPasswordScreen.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(AppScreens.CreateAdvertisementScreen.route) {
            CreateAdvertisementScreen(navController = navController)
        }
        composable(AppScreens.AdvertisementPaymentScreen.route) {
            AdvertisementPaymentScreen(navController = navController)
        }
        composable(AppScreens.AdvertisementSuccessScreen.route) {
            AdvertisementSuccessScreen(navController = navController)
        }
        composable(
            route = AppScreens.AdvertisementDetailScreen.route,
            arguments = listOf(navArgument("advertisementId") { type = NavType.StringType })
        ) {
            val advertisementId = it.arguments?.getString("advertisementId") ?: ""
            AdvertisementDetailScreen(navController = navController)
        }
        composable(route = AppScreens.ExerciseManagementScreen.route) {
            ExerciseManagementScreen(navController = navController)
        }
        composable(route = AppScreens.AddExerciseScreen.route) {
            AddExerciseScreen(navController = navController)
        }
        composable(route = AppScreens.ExerciseCategoriesScreen.route) {
            ExerciseManagementScreen(navController = navController)
        }
        composable(route = AppScreens.CreateExercisePlanScreen.route) {
            CreateExercisePlanScreen(navController = navController)
        }
        composable(
            route = AppScreens.EditExerciseScreen.route,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId") ?: ""
            EditExerciseScreen(
                navController = navController,
                exerciseId = exerciseId
            )
        }
        composable(route = AppScreens.UserExercisePlansScreen.route) {
            UserExercisePlansScreen(navController = navController)
        }
        composable(
            route = AppScreens.EditExercisePlanScreen.route,
            arguments = listOf(navArgument("planId") { type = NavType.StringType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            EditExercisePlanScreen(navController = navController, planId = planId)
        }
        composable(
            route = AppScreens.ExercisePlanDetailScreen.route,
            arguments = listOf(navArgument("planId") { type = NavType.StringType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: ""
            ExercisePlanDetailScreen(
                navController = navController,
                planId = planId
            )
        }
    }
}