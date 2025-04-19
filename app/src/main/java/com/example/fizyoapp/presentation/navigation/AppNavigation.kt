package com.example.fizyoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fizyoapp.presentation.login.LoginScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_main_screen.PhysiotherapistMainScreen
import com.example.fizyoapp.presentation.register.RegisterScreen
import com.example.fizyoapp.presentation.user.usermainscreen.UserMainScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.paylasimlarscreen.PaylasimlarScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.profilscreen.ProfilScreen
import com.example.fizyoapp.presentation.bottomnavbar.items.searchscreen.SearchScreen
import com.example.fizyoapp.presentation.physiotherapistdetail.PhysiotherapistDetailScreen
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_profile_screen.PhysiotherapistProfileSetupScreen
import com.example.fizyoapp.presentation.user.buttons.hastaliklarim.HastaliklarimScreen
import com.example.fizyoapp.presentation.user.buttons.ornekegzersizler.OrnekEgzersizler
import com.example.fizyoapp.presentation.splashscreen.SplashScreen
import com.example.fizyoapp.presentation.user.buttons.hastaliklarim.radyolojikgoruntuekle.RadyolojikGoruntuEkle
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



    }
}