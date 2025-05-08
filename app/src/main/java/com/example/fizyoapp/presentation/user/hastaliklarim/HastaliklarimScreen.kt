package com.example.fizyoapp.presentation.user.hastaliklarim

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fizyoapp.presentation.ui.bottomnavbar.BottomNavbarComponent
import com.example.fizyoapp.presentation.user.hastaliklarim.radyolojikgoruntuekle.RadyolojikGoruntuEkle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HastaliklarimScreen(navController: NavController) {


    Scaffold(
        bottomBar = { BottomNavbarComponent(navController) }
    ) {
            LazyColumn(
            modifier = Modifier.fillMaxSize()
                .background(Color(0xff06164c)),verticalArrangement = Arrangement.Center

        ) {
                item{
                    RadyolojikGoruntuEkle(navController)
                }

        }
    }
}




    // radyolojik görüntü,doktor raporu,diyabet vs,kronik hastalık vs,anamnez formları, olacak.
    //rrehab geçmişinde ise randevu aldığında ve tarihi geçtiğğinde otomatik veri gelicek.
    //fztnin yazdıkları, egzerislzeri fzt notları olacak.zoom linki saat tarih ve kayıtları fzt bilgileri olacak.



@Preview
@Composable
fun PrevHastaliklarim(){
    HastaliklarimScreen(navController = rememberNavController())
}