package com.example.fizyoapp.presentation.appointment.calendar

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.data.util.capitalize
import com.example.fizyoapp.domain.model.appointment.Appointment
import com.example.fizyoapp.presentation.appointment.booking.CalendarView
import com.example.fizyoapp.presentation.appointment.booking.TimeSlotGrid
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysiotherapistCalendarScreen(
    navController: NavController,
    viewModel: PhysiotherapistCalendarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val primaryColor = Color(0xFF3B3E68)
    val accentColor = Color(0xFF6D72C3)
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    var showRehabilitationDialog by remember { mutableStateOf(false) }
    var showUserDetailsDialog by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }


    var showBlockConfirmationDialog by remember { mutableStateOf(false) }
    var timeSlotToBlock by remember { mutableStateOf<String?>(null) }

    var selectedMonth by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Takvimim") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { viewModel.onEvent(PhysiotherapistCalendarEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = accentColor
                )
            } else if (state.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.Center)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFB71C1C),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.error!!,
                                style = LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFFB71C1C)
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.onEvent(PhysiotherapistCalendarEvent.Refresh) },
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
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tarih Seçin",
                            style = LocalTextStyle.current.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = primaryColor
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (selectedMonth > 0) selectedMonth--
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Önceki Ay",
                                    tint = primaryColor
                                )
                            }
                            val currentCalendar = Calendar.getInstance()
                            currentCalendar.add(Calendar.MONTH, selectedMonth)
                            val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale("tr"))
                            Text(
                                text = monthYearFormat.format(currentCalendar.time).capitalize(),
                                style = LocalTextStyle.current.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = primaryColor
                                )
                            )
                            IconButton(
                                onClick = {
                                    if (selectedMonth < 5) selectedMonth++ // Maksimum 5 ay ileri
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Sonraki Ay",
                                    tint = primaryColor
                                )
                            }
                        }
                    }

                    CalendarView(
                        currentMonth = selectedMonth,
                        onDateSelected = { date ->
                            viewModel.onEvent(PhysiotherapistCalendarEvent.DateSelected(date))
                        },
                        selectedDate = state.selectedDate
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Bugünkü Randevular",
                        style = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = primaryColor
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    val selectedDate = state.selectedDate
                    val todayAppointments = if (selectedDate != null) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        state.appointments.filter {
                            dateFormat.format(it.date) == dateFormat.format(selectedDate)
                        }
                    } else {
                        emptyList()
                    }
                    if (todayAppointments.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Bugün için randevu bulunmamaktadır.",
                                    style = LocalTextStyle.current.copy(
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    } else {
                        todayAppointments.forEach { appointment ->
                            AppointmentCard(
                                appointment = appointment,
                                onInfoClick = {
                                    selectedAppointment = appointment
                                    showRehabilitationDialog = true
                                },
                                onUserDetailsClick = { userId ->
                                    selectedUserId = userId
                                    showUserDetailsDialog = true
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Müsait Saatler",
                        style = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = primaryColor
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    if (state.availableTimeSlots.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tüm saatler dolu veya bloke edilmiş.",
                                    style = LocalTextStyle.current.copy(
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Tıklayarak saatleri bloke edebilirsiniz:",
                            style = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                color = Color.Gray
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        TimeSlotGrid(
                            timeSlots = state.availableTimeSlots,
                            selectedTimeSlot = null,
                            onTimeSlotSelected = { timeSlot ->
                                timeSlotToBlock = timeSlot
                                showBlockConfirmationDialog = true
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(60.dp)) // Bottom padding for scrolling
                }
            }

            if (state.success != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = {
                            viewModel.onEvent(PhysiotherapistCalendarEvent.Refresh)
                        }) {
                            Text(
                                text = "Tamam",
                                style = LocalTextStyle.current.copy(color = Color.White)
                            )
                        }
                    },
                    containerColor = Color(0xFF43A047)
                ) {
                    Text(
                        text = state.success!!,
                        style = LocalTextStyle.current.copy(color = Color.White)
                    )
                }
            }
        }
    }

    if (showRehabilitationDialog && selectedAppointment != null) {
        RehabilitationNoteDialog(
            appointment = selectedAppointment!!,
            onDismiss = { showRehabilitationDialog = false },
            onSave = { notes ->
                viewModel.updateRehabilitationNotes(selectedAppointment!!.id, notes)
                showRehabilitationDialog = false
            }
        )
    }

    if (showUserDetailsDialog && selectedUserId != null) {
        UserDetailsDialog(
            userId = selectedUserId!!,
            onDismiss = { showUserDetailsDialog = false }
        )
    }


    if (showBlockConfirmationDialog && timeSlotToBlock != null) {
        TimeBlockConfirmationDialog(
            timeSlot = timeSlotToBlock!!,
            onConfirm = {
                val reason = "Manuel olarak bloke edildi"
                viewModel.onEvent(PhysiotherapistCalendarEvent.BlockTimeSlot(timeSlotToBlock!!, reason))
                showBlockConfirmationDialog = false
                timeSlotToBlock = null
            },
            onDismiss = {
                showBlockConfirmationDialog = false
                timeSlotToBlock = null
            }
        )
    }
}

@Composable
fun TimeBlockConfirmationDialog(
    timeSlot: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Saat Bloke Etme")
        },
        text = {
            Text("$timeSlot saatini bloke etmek istiyor musunuz?")
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text("Evet")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Hayır")
            }
        }
    )
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    onInfoClick: (Appointment) -> Unit,
    onUserDetailsClick: (String) -> Unit
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
                    if (appointment.userPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = appointment.userPhotoUrl,
                            contentDescription = "Profil fotoğrafı",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profil",
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
                        text = appointment.userName.ifEmpty { "İsimsiz Hasta" },
                        style = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = primaryColor
                        )
                    )
                    Text(
                        text = "${SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(appointment.date)}, ${appointment.timeSlot}",
                        style = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    )

                    val typeText = when (appointment.appointmentType) {
                        com.example.fizyoapp.domain.model.appointment.AppointmentType.IN_PERSON -> "Yüz Yüze Randevu"
                        com.example.fizyoapp.domain.model.appointment.AppointmentType.REMOTE -> "Uzaktan Randevu"
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

                Row {

                    IconButton(
                        onClick = { onInfoClick(appointment) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Bilgilendirme",
                            tint = primaryColor
                        )
                    }

                    IconButton(
                        onClick = { onUserDetailsClick(appointment.userId) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Kullanıcı Bilgileri",
                            tint = primaryColor
                        )
                    }
                }
            }

            if (appointment.rehabilitationNotes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rehabilitasyon Notları:",
                    style = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = appointment.rehabilitationNotes,
                    style = LocalTextStyle.current.copy(
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
fun RehabilitationNoteDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var noteText by remember { mutableStateOf(appointment.rehabilitationNotes) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Rehabilitasyon Notu Ekle",
                style = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Hasta: ${appointment.userName}",
                    style = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "Tarih: ${SimpleDateFormat("dd MMM yyyy", Locale("tr")).format(appointment.date)}, ${appointment.timeSlot}",
                    style = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Rehabilitasyon notları") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(noteText) }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun UserDetailsDialog(
    userId: String,
    onDismiss: () -> Unit,
    calenderUserDetailsViewModel: CalenderUserDetailsViewModel = hiltViewModel()
) {
    val userDetailsState by calenderUserDetailsViewModel.state.collectAsState()
    LaunchedEffect(userId) {
        calenderUserDetailsViewModel.getUserProfile(userId)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Kullanıcı Bilgileri",
                style = LocalTextStyle.current.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            if (userDetailsState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (userDetailsState.error != null) {
                Text(
                    text = "Hata: ${userDetailsState.error}",
                    style = LocalTextStyle.current.copy(
                        color = Color.Red
                    )
                )
            } else {
                val profile = userDetailsState.userProfile
                Column {
                    if (profile?.profilePhotoUrl?.isNotEmpty() == true) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            AsyncImage(
                                model = profile.profilePhotoUrl,
                                contentDescription = "Profil fotoğrafı",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    UserInfoItem(
                        label = "Ad Soyad",
                        value = "${profile?.firstName} ${profile?.lastName}"
                    )
                    UserInfoItem(
                        label = "Cinsiyet",
                        value = profile?.gender ?: ""
                    )
                    UserInfoItem(
                        label = "Doğum Tarihi",
                        value = profile?.birthDate?.let {
                            SimpleDateFormat("dd.MM.yyyy", Locale("tr")).format(it)
                        } ?: ""
                    )
                    UserInfoItem(
                        label = "Telefon",
                        value = profile?.phoneNumber ?: ""
                    )
                    UserInfoItem(
                        label = "Konum",
                        value = "${profile?.city} / ${profile?.district}"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss
            ) {
                Text("Tamam")
            }
        }
    )
}

@Composable
fun UserInfoItem(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = LocalTextStyle.current.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Gray
            )
        )
        Text(
            text = value,
            style = LocalTextStyle.current.copy(
                fontSize = 16.sp
            )
        )
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}