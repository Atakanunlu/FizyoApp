package com.example.fizyoapp.presentation.physiotherapist.physiotherapistdetail

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.theme.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysiotherapistDetailScreen(
    navController: NavController,
    viewModel: PhysiotherapistDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fizyoterapist Profili") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = primaryColor,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(60.dp)
                    )
                }
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = errorColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error ?: "Bir hata oluştu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { /* Yeniden yükleme işlemi için viewModel'deki ilgili metod çağrılabilir */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tekrar Dene")
                    }
                }
            } else {
                state.physiotherapist?.let { physiotherapist ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (physiotherapist.profilePhotoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model = physiotherapist.profilePhotoUrl,
                                    contentDescription = "Profil fotoğrafı",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profil",
                                    tint = primaryColor,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "FZT. ${physiotherapist.firstName} ${physiotherapist.lastName}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "${physiotherapist.city} / ${physiotherapist.district}",
                            fontSize = 16.sp,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        PhysiotherapistInfoCard(
                            title = "İletişim Bilgileri",
                            icon = Icons.Default.ContactPhone,
                            content = {
                                Column {
                                    DetailRow(
                                        icon = Icons.Default.Phone,
                                        text = physiotherapist.phoneNumber
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    DetailRow(
                                        icon = Icons.Default.LocationOn,
                                        text = physiotherapist.fullAddress
                                    )
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (physiotherapist.certificates.isNotEmpty()) {
                            PhysiotherapistInfoCard(
                                title = "Sertifikalar ve Uzmanlıklar",
                                icon = Icons.Default.VerifiedUser,
                                content = {
                                    Column {
                                        physiotherapist.certificates.forEach { certificate ->
                                            DetailRow(
                                                icon = Icons.Default.CheckCircle,
                                                text = certificate
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        PhysiotherapistInfoCard(
                            title = "Fiyat Bilgisi",
                            icon = Icons.Default.Payments,
                            content = {
                                Text(
                                    text = physiotherapist.priceInfo,
                                    fontSize = 16.sp,
                                    color = textColor
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = accentColor.copy(alpha = 0.2f)
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 0.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Randevu oluşturarak fizyoterapist ile görüşme planlayabilirsiniz.",
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val physiotherapistId = state.physiotherapist?.userId ?: ""
                                if (physiotherapistId.isNotEmpty()) {
                                    val route = AppScreens.AppointmentBookingScreen.createRoute(physiotherapistId)
                                    navController.navigate(route)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Randevu Oluştur",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val physiotherapistId = state.physiotherapist?.userId ?: ""
                                if (physiotherapistId.isNotEmpty()) {
                                    val route = AppScreens.MessagesDetailScreen.createMessageDetailRoute(physiotherapistId)
                                    navController.navigate(route)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = primaryColor
                            ),
                            border = BorderStroke(1.dp, primaryColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mesaj Gönder",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PhysiotherapistInfoCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 16.sp,
            color = textColor
        )
    }
}