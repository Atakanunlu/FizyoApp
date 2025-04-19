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

}