package com.example.fizyoapp.presentation.user.rehabilitation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.fizyoapp.domain.model.appointment.AppointmentType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RehabilitationHistoryScreen(
    navController: NavController,
    viewModel: RehabilitationHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val primaryColor = Color(0xFF3B3E68)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rehabilitasyon Geçmişim") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.error ?: "Bir hata oluştu",
                        style = LocalTextStyle.current.copy(
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.loadAppointments() }
                    ) {
                        Text("Yeniden Dene")
                    }
                }
            } else if (state.appointments.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Henüz rehabilitasyon kaydınız bulunmuyor",
                        style = LocalTextStyle.current.copy(
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(state.appointments) { appointment ->
                        RehabilitationHistoryCard(appointment = appointment)
                    }
                }
            }
        }
    }
}

@Composable
fun RehabilitationHistoryCard(
    appointment: AppointmentWithPhysiotherapist
) {
    val primaryColor = Color(0xFF3B3E68)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (appointment.physiotherapistPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = appointment.physiotherapistPhotoUrl,
                            contentDescription = "Fizyoterapist fotoğrafı",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Fizyoterapist",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "FZT. ${appointment.physiotherapistName}",
                        style = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = primaryColor
                        )
                    )

                    Text(
                        text = "${SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(appointment.appointment.date)}, ${appointment.appointment.timeSlot}",
                        style = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    )

                    val typeText = when (appointment.appointment.appointmentType) {
                        AppointmentType.IN_PERSON -> "Yüz Yüze Randevu"
                        AppointmentType.REMOTE -> "Uzaktan Randevu"
                    }

                    Text(
                        text = typeText,
                        style = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor
                        )
                    )
                }
            }

            if (appointment.appointment.rehabilitationNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Rehabilitasyon Notları:",
                    style = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = appointment.appointment.rehabilitationNotes,
                    style = LocalTextStyle.current.copy(
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}