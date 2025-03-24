package com.example.fizyoapp.presentation.navigation

sealed class AppScreens(val route:String) {
    object SplashScreen: AppScreens("splash_screen")
    object LoginScreen: AppScreens("login_screen")
    object RegisterScreen: AppScreens("register_screen")
    object PhysiotherapistMainScreen: AppScreens("physiotherapist_main_screen")
    object UserMainScreen: AppScreens("user_main_screen")
}