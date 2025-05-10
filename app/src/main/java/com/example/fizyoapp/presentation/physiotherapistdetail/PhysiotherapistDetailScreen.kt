package com.example.fizyoapp.presentation.physiotherapistdetail

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.presentation.navigation.AppScreens

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysiotherapistDetailScreen(
    navController: NavController,
    viewModel: PhysiotherapistDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    // Modern tema renkleri
    val primaryColor = Color(0xFF3B3E68)
    val backgroundColor = Color(0xFFF8F9FC)
    val accentColor = Color(0xFF6D72C3)
    val cardColor = Color.White

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Fizyoterapist Detayı",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    if (state.physiotherapist != null) {
                        IconButton(onClick = {
                            // Şu anki fizyoterapist ile mesajlaşma ekranına git
                            navController.navigate(
                                AppScreens.MessagesDetailScreen.createMessageDetailRoute(
                                    state.physiotherapist!!.userId
                                )
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Mesaj Gönder",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .background(backgroundColor)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = accentColor,
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(50.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Fizyoterapist bilgileri yükleniyor...",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(70.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.error ?: "Bir hata oluştu",
                                color = Color.DarkGray,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { /* Retry logic */ },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tekrar Dene")
                            }
                        }
                    }
                }

                else -> {
                    state.physiotherapist?.let { physiotherapist ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            // Üst profil kartı
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            ) {
                                // Arka plan banner
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    primaryColor,
                                                    accentColor
                                                )
                                            )
                                        )
                                )

                                // Profil içeriği
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 30.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Profil resmi
                                    Card(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .shadow(10.dp, CircleShape),
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(
                                            containerColor = cardColor
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (physiotherapist.profilePhotoUrl.isNotEmpty()) {
                                                AsyncImage(
                                                    model = physiotherapist.profilePhotoUrl,
                                                    contentDescription = "Profil fotoğrafı",
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .background(
                                                            brush = Brush.linearGradient(
                                                                colors = listOf(
                                                                    primaryColor,
                                                                    accentColor
                                                                )
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "Profil",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(70.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // İsim
                                    Text(
                                        text = "FZT. ${physiotherapist.firstName} ${physiotherapist.lastName}",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.DarkGray
                                    )

                                    // Konum
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = accentColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${physiotherapist.city} / ${physiotherapist.district}",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            // İletişim kartı
                            InfoCard(
                                title = "İletişim Bilgileri",
                                icon = Icons.Default.ContactPhone,
                                accentColor = accentColor,
                                cardColor = cardColor
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 12.dp)
                                ) {
                                    ContactInfoRow(
                                        icon = Icons.Default.Phone,
                                        text = physiotherapist.phoneNumber,
                                        accentColor = accentColor
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    ContactInfoRow(
                                        icon = Icons.Default.LocationOn,
                                        text = physiotherapist.fullAddress,
                                        accentColor = accentColor
                                    )
                                }
                            }

                            // Sertifikalar kartı
                            if (physiotherapist.certificates.isNotEmpty()) {
                                InfoCard(
                                    title = "Sertifikalar",
                                    icon = Icons.Default.WorkspacePremium,
                                    accentColor = accentColor,
                                    cardColor = cardColor
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 8.dp)
                                    ) {
                                        physiotherapist.certificates.forEach { certificate ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(vertical = 6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = accentColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = certificate,
                                                    fontSize = 15.sp,
                                                    color = Color.DarkGray
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Fiyat bilgisi kartı
                            InfoCard(
                                title = "Fiyat Bilgisi",
                                icon = Icons.Default.Payments,
                                accentColor = accentColor,
                                cardColor = cardColor
                            ) {
                                Text(
                                    text = physiotherapist.priceInfo,
                                    fontSize = 15.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                                )
                            }

                            // Mesaj gönder butonu
                            Button(
                                onClick = {
                                    navController.navigate(
                                        AppScreens.MessagesDetailScreen.createMessageDetailRoute(
                                            physiotherapist.userId
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Mesaj Gönder",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    cardColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Başlık
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(accentColor.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            // İçerik
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun ContactInfoRow(
    icon: ImageVector,
    text: String,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = Color.DarkGray
        )
    }
}