package com.example.fizyoapp.ui.splashscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimatable
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.fizyoapp.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController){

    val isPlaying by remember{ mutableStateOf(true) }
    val speed by remember{ mutableFloatStateOf(1f) }
    val composition by rememberLottieComposition(
        spec= LottieCompositionSpec.RawRes(R.raw.animation_splash)
    )

    val progress by animateLottieCompositionAsState(
        composition=composition,
        speed=speed,
        isPlaying = isPlaying,
        iterations =LottieConstants.IterateForever,
        restartOnPlay =false

    )

    LaunchedEffect(Unit) {
        delay(1000)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.White),
        contentAlignment = Alignment.Center){

        LottieAnimation(
            composition=composition,
            progress= { progress },
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.Center

        )

    }
}

@Preview
@Composable
fun PrevSplash(){
    SplashScreen(navController = rememberNavController())
}