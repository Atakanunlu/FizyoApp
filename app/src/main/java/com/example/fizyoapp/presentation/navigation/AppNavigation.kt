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
    }
}