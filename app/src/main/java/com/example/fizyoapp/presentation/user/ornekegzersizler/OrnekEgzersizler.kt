package com.example.fizyoapp.presentation.user.ornekegzersizler

import android.annotation.SuppressLint

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fizyoapp.R
import com.example.fizyoapp.ui.bottomnavbar.BottomNavbarComponent

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OrnekEgzersizler(navController: NavController) {

    val ornekEgzersizlerGiris = listOf(
        "Omuz Egzersizleri" to R.drawable.omuz,
        "Boyun Egzersizleri" to R.drawable.neck,
        "Bel Egzersizleri" to R.drawable.bel,
        "Bacak Egzersizleri" to R.drawable.bacak,
        "Core Egzersizleri" to R.drawable.core,
        "Kalça Egzersizleri" to R.drawable.hip
    )

    Scaffold(bottomBar = { BottomNavbarComponent(navController) }) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(59, 62, 104)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(ornekEgzersizlerGiris.size) { index ->
                val (title, imageRes) = ornekEgzersizlerGiris[index]
                Box() {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(vertical = 15.dp, horizontal = 30.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .height(130.dp)
                    )
                    Text(
                        text = title,
                        style = TextStyle(Color.DarkGray, fontSize = 25.sp, fontStyle = FontStyle.Italic),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // BottomNavbarComponent için yeterli alan
            }
        }
    }
}


@Preview
@Composable
fun PrevOrnekEX(){
    OrnekEgzersizler(navController = rememberNavController())
}
