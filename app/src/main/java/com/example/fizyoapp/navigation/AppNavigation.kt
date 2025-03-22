package com.example.fizyoapp.navigation

import RadyolojikGoruntuEkle
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fizyoapp.ui.bottomnavbar.items.paylasimlarscreen.PaylasimlarScreen
import com.example.fizyoapp.ui.bottomnavbar.items.profilscreen.ProfilScreen
import com.example.fizyoapp.ui.bottomnavbar.items.searchscreen.SearchScreen
import com.example.fizyoapp.ui.mainscreen.MainScreen
import com.example.fizyoapp.ui.mainscreen.buttons.hastaliklarim.HastaliklarimScreen

import com.example.fizyoapp.ui.mainscreen.buttons.ornekegzersizler.OrnekEgzersizler
import com.example.fizyoapp.ui.splashscreen.SplashScreen

@Composable
fun AppNavigation(){
    val navController = rememberNavController()

    NavHost(
        navController=navController, startDestination = AppScreens.SplashScreen.route){
        composable(AppScreens.SplashScreen.route) {
            SplashScreen(navController = navController)
        }

        composable(AppScreens.MainScreen.route){
            MainScreen(navController=navController)
        }
        composable(AppScreens.OrnekEgzersizler.route){
            OrnekEgzersizler(navController=navController)
        }
        composable(AppScreens.SearchScreen.route){
            SearchScreen(navController=navController)
        }
        composable(AppScreens.ProfilScreen.route){
            ProfilScreen(navController=navController)
        }
        composable(AppScreens.PaylasimlarScreen.route) {
            PaylasimlarScreen(navController=navController)
        }
        composable(AppScreens.HastaliklarimScreen.route) {
            HastaliklarimScreen(navController=navController)
        }
        composable(AppScreens.RadyolojikGoruntuEkleScreen.route){
            RadyolojikGoruntuEkle(navController=navController)
        }

    }

}
