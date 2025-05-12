package com.example.fizyoapp.presentation.bottomnavbar.items.paylasimlarscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fizyoapp.R
import com.example.fizyoapp.presentation.ui.bottomnavbar.BottomNavbarComponent
import com.example.fizyoapp.presentation.ui.bottomnavbar.BottomNavbarComponent



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PaylasimlarScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavbarComponent(navController)}

    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row {

                        Image(
                            painter = painterResource(R.drawable.hip),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .size(60.dp)

                        )

                        Text(
                            text = "Paylaşan Kişi",
                            modifier = Modifier.padding()
                        )

                    }
                    Spacer(modifier = Modifier.padding(15.dp))


                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.core),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }

                    Text(
                        text = "FOTOĞRAF AÇIKLAMASI",
                        modifier = Modifier
                            .padding(top = 10.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PaylasimlarPrev() {
    PaylasimlarScreen(navController = rememberNavController())
}
