package com.example.fizyoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fizyoapp.presentation.login.LoginScreen
import com.example.fizyoapp.presentation.physiotherapist.PhysiotherapistMainScreen
import com.example.fizyoapp.presentation.register.RegisterScreen
import com.example.fizyoapp.presentation.ui.splashscreen.SplashScreen
import com.example.fizyoapp.presentation.user.UserMainScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(
        navController=navController,
        startDestination = AppScreens.SplashScreen.route){
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

    }

}
