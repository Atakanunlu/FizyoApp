package com.example.fizyoapp.navigation

sealed class AppScreens(val route:String) {
    object SplashScreen: AppScreens("splash_screen")
    object MainScreen:AppScreens("main_screen")
    object OrnekEgzersizler:AppScreens("ornek_egzersizler")
    object SearchScreen:AppScreens("search_screen")
    object ProfilScreen:AppScreens("profil_screen")
    object PaylasimlarScreen:AppScreens("paylasimlar_screen")
    object HastaliklarimScreen:AppScreens("hastaliklarim_screen")
    object RadyolojikGoruntuEkleScreen:AppScreens("radyolojik_goruntu_ekle_screen")
}