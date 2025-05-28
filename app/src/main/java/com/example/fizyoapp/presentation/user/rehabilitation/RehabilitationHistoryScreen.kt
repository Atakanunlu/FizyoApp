package com.example.fizyoapp.presentation.user.rehabilitation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.appointment.AppointmentStatus
import com.example.fizyoapp.domain.model.appointment.AppointmentType
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RehabilitationHistoryScreen(
    navController: NavController,
    viewModel: RehabilitationHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var showCancelDialog by remember { mutableStateOf(false) }
    var appointmentToCancel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(RehabilitationHistoryEvent.Refresh)
    }

    LaunchedEffect(Unit) {
        val navBackStackEntry = navController.currentBackStackEntry
        navBackStackEntry?.lifecycle?.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
            override fun onResume(owner: androidx.lifecycle.LifecycleOwner) {
                viewModel.onEvent(RehabilitationHistoryEvent.Refresh)
            }
        })
    }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            delay(3000)
            viewModel.clearSuccessMessage()
        }
    }

    if (showCancelDialog && appointmentToCancel != null) {
        AlertDialog(
            onDismissRequest = {
                showCancelDialog = false
                appointmentToCancel = null
            },
            title = { Text("Randevu İptali") },
            text = { Text("Bu randevuyu iptal etmek istediğinizden emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        appointmentToCancel?.let { id ->
                            viewModel.onEvent(RehabilitationHistoryEvent.CancelAppointment(id))
                        }
                        showCancelDialog = false
                        appointmentToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Evet, İptal Et")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    appointmentToCancel = null
                }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rehabilitasyon Geçmişim") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.onEvent(RehabilitationHistoryEvent.Refresh)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3B3E68),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF3B3E68)
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
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.onEvent(RehabilitationHistoryEvent.Refresh) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B3E68))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tekrar Dene")
                    }
                }
            } else if (state.upcomingAppointments.isEmpty() && state.pastAppointments.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz bir randevunuz bulunmuyor",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bir fizyoterapist seçerek randevu alabilirsiniz",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (state.upcomingAppointments.isNotEmpty()) {
                        item {
                            Text(
                                text = "Yaklaşan Randevular",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B3E68),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(state.upcomingAppointments) { appointmentWithPhysiotherapist ->
                            AppointmentCard(
                                appointmentWithPhysiotherapist = appointmentWithPhysiotherapist,
                                isPast = false,
                                onCancelClick = { appointmentId ->
                                    appointmentToCancel = appointmentId
                                    showCancelDialog = true
                                }
                            )
                        }
                    }

                    if (state.pastAppointments.isNotEmpty()) {
                        item {
                            Text(
                                text = "Geçmiş Randevular",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B3E68),
                                modifier = Modifier.padding(vertical = 8.dp)
                                    .padding(top = 24.dp)
                            )
                        }

                        items(state.pastAppointments) { appointmentWithPhysiotherapist ->
                            AppointmentCard(
                                appointmentWithPhysiotherapist = appointmentWithPhysiotherapist,
                                isPast = true,
                                onCancelClick = { }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            AnimatedVisibility(
                visible = state.successMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    color = Color(0xFF4CAF50),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = state.successMessage ?: "",
                            color = Color.White
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.error != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    color = Color(0xFFB71C1C),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = state.error ?: "",
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppointmentCard(
    appointmentWithPhysiotherapist: AppointmentWithPhysiotherapist,
    isPast: Boolean,
    onCancelClick: (String) -> Unit
) {
    val appointment = appointmentWithPhysiotherapist.appointment
    val isCancelled = appointment.status == AppointmentStatus.CANCELLED
    val backgroundColor = when {
        isCancelled -> Color(0xFFFFEBEE)
        isPast -> Color(0xFFF5F5F5)
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isCancelled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val cancelText = if (appointment.cancelledBy == "physiotherapist")
                        "Fizyoterapist tarafından iptal edildi"
                    else
                        "Tarafınızca iptal edildi"
                    Text(
                        text = cancelText,
                        fontSize = 14.sp,
                        color = Color.Red,
                        fontStyle = FontStyle.Italic
                    )
                }
                Divider(color = Color.Red.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B3E68).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (appointmentWithPhysiotherapist.physiotherapistPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = appointmentWithPhysiotherapist.physiotherapistPhotoUrl,
                            contentDescription = "Fizyoterapist",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Fizyoterapist",
                            tint = Color(0xFF3B3E68),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = appointmentWithPhysiotherapist.physiotherapistName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCancelled) Color.Gray else Color(0xFF3B3E68)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = SimpleDateFormat("dd MMMM yyyy", Locale("tr")).format(appointment.date),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = appointment.timeSlot,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (appointment.appointmentType) {
                            AppointmentType.IN_PERSON -> Icons.Default.Person
                            AppointmentType.REMOTE -> Icons.Default.Videocam
                        }
                        val typeText = when (appointment.appointmentType) {
                            AppointmentType.IN_PERSON -> "Yüz Yüze Randevu"
                            AppointmentType.REMOTE -> "Uzaktan Randevu"
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = typeText,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (!isPast && !isCancelled) {
                    IconButton(
                        onClick = { onCancelClick(appointment.id) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "İptal Et",
                            tint = Color.Red
                        )
                    }
                }
            }

            if (appointment.rehabilitationNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Rehabilitasyon Notları:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCancelled) Color.Gray else Color(0xFF3B3E68)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = appointment.rehabilitationNotes,
                    fontSize = 14.sp,
                    color = if (isCancelled) Color.Gray else Color.DarkGray
                )
            }

            if (isCancelled && appointment.cancelledAt != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "İptal tarihi: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr")).format(appointment.cancelledAt)}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}