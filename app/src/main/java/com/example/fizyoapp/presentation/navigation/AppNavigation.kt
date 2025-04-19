package com.example.fizyoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fizyoapp.presentation.login.LoginScreen
import com.example.fizyoapp.presentation.physiotherapist.PhysiotherapistMainScreen
import com.example.fizyoapp.presentation.register.RegisterScreen
import com.example.fizyoapp.presentation.user.usermainscreen.UserMainScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.paylasimlarscreen.PaylasimlarScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.profilscreen.ProfilScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.searchscreen.SearchScreen
import com.example.fizyoapp.presentation.user.hastaliklarim.HastaliklarimScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.OrnekEgzersizler
import com.example.fizyoapp.ui.splashscreen.SplashScreen
import com.example.fizyoapp.presentation.user.hastaliklarim.radyolojikgoruntuekle.RadyolojikGoruntuEkle
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.CoreExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.LegExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.LowerBackExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.NeckExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.core.ShoulderExercisesScreen
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.hip.HipExercisesScreen

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


    }
}