package com.example.fizyoapp.presentation.user.usermainscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.bottomnavbar.BottomNavbarComponent


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserMainScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UserViewModel.UiEvent.NavigateToLogin -> {
                    navController.navigate("login_screen") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavbarComponent(navController) }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(59, 62, 104))
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .padding(bottom = 80.dp), // Bottom navigation bar için padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Exercizlerim Butonu
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Button(
                        onClick = { /* Egzersizlerim sayfasına navigasyon */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                            .height(150.dp),
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 10.dp,
                            pressedElevation = 6.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        )
                    ) {
                        Text(
                            text = "EGZERSİZLERİM",
                            fontStyle = FontStyle.Italic,
                            style = TextStyle(fontSize = 20.sp)
                        )
                        Icon(
                            imageVector = Icons.Filled.AccessibilityNew,
                            contentDescription = null,
                            Modifier.padding(start = 17.dp)
                        )
                    }
                }

                // Orta Butonlar
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { navController.navigate(AppScreens.OrnekEgzersizler.route) },
                        modifier = Modifier
                            .height(150.dp)
                            .weight(1f)
                            .padding(start = 5.dp, end = 10.dp),
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 10.dp,
                            pressedElevation = 6.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        )
                    ) {
                        Text(
                            text = "Örnek Egzersizler ",
                            style = TextStyle(fontSize = 17.sp),
                            fontStyle = FontStyle.Italic
                        )
                        Icon(
                            imageVector = Icons.Filled.AccessibilityNew,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Button(
                        onClick = { navController.navigate(AppScreens.HastaliklarimScreen.route) },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.DarkGray,
                            containerColor = Color.White
                        ),
                        modifier = Modifier
                            .height(150.dp)
                            .weight(1f)
                            .padding(start = 5.dp, end = 5.dp),
                        shape = RoundedCornerShape(10.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 10.dp,
                            pressedElevation = 6.dp
                        )
                    ) {
                        Text(
                            text = "Hastalıklarım ",
                            fontStyle = FontStyle.Italic,
                            style = TextStyle(fontSize = 19.sp)
                        )
                        Icon(
                            imageVector = Icons.Filled.Healing,
                            contentDescription = null,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                // Rehabilitasyon Geçmişim Butonu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { /* Rehabilitasyon Geçmişi sayfasına navigasyon */ },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.DarkGray
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 10.dp,
                            pressedElevation = 6.dp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                            .height(150.dp),
                    ) {
                        Text(
                            text = "Rehabilitasyon Geçmişim  ",
                            fontStyle = FontStyle.Italic,
                            style = TextStyle(fontSize = 20.sp)
                        )
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

            }


            item {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp),
                        color = Color.White
                    )
                }
            }
        }
    }
}