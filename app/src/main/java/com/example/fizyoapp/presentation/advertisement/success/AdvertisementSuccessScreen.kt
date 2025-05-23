package com.example.fizyoapp.presentation.advertisement.success

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.navigation.AppScreens

@Composable
fun AdvertisementSuccessScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberVectorPainter(Icons.Default.CheckCircle),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Green)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ödeme Başarılı!",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Reklamınız başarıyla oluşturuldu ve 24 saat boyunca aktif olacak.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                navController.navigate(AppScreens.PhysiotherapistMainScreen.route) {
                    popUpTo(AppScreens.PhysiotherapistMainScreen.route) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Ana Sayfaya Dön")
        }
    }
}