package com.example.fizyoapp.presentation.splashscreen

import com.example.fizyoapp.domain.model.auth.UserRole
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.fizyoapp.R
import com.example.fizyoapp.presentation.navigation.AppScreens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.animation_splash)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = true,
        iterations = LottieConstants.IterateForever,
        restartOnPlay = false
    )

    LaunchedEffect(
        state.isUserLoggedIn,
        state.isProfileCompleted,
        state.userRole,
        state.isLoading
    ) {
        if (!state.isLoading) {
            delay(2000)

            if (state.isUserLoggedIn) {
                if (!state.isProfileCompleted) {

                    when (state.userRole) {
                        UserRole.USER -> navController.navigate(AppScreens.UserProfileSetupScreen.route) {
                            popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
                        }
                        UserRole.PHYSIOTHERAPIST -> navController.navigate(AppScreens.PhysiotherapistProfileSetupScreen.route) {
                            popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
                        }
                        else -> navController.navigate(AppScreens.LoginScreen.route) {
                            popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
                        }
                    }
                } else {

                    when (state.userRole) {
                        UserRole.USER -> navController.navigate(AppScreens.UserMainScreen.route) {
                            popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
                        }
                        UserRole.PHYSIOTHERAPIST -> navController.navigate(AppScreens.PhysiotherapistMainScreen.route) {
                            popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
                        }
                        else -> navController.navigate(AppScreens.LoginScreen.route) {
                            popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
                        }
                    }
                }
            } else {

                navController.navigate(AppScreens.LoginScreen.route) {
                    popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
                }
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.Center
        )
    }
}