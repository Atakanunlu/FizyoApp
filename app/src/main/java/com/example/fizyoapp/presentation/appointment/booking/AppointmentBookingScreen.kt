package com.example.fizyoapp.presentation.appointment.booking

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.data.util.capitalize
import com.example.fizyoapp.domain.model.appointment.AppointmentType
import com.example.fizyoapp.presentation.navigation.AppScreens
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentBookingScreen(
    navController: NavController,
    viewModel: AppointmentBookingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val primaryColor = Color(0xFF3B3E68)
    val accentColor = Color(0xFF6D72C3)

    LaunchedEffect(key1 = viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AppointmentBookingViewModel.UiEvent.AppointmentBooked -> {
                    navController.navigate(AppScreens.UserMainScreen.route) {
                        popUpTo(AppScreens.PhysiotherapistDetailScreen.route) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Randevu Oluştur") },
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
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            if (state.isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(60.dp),
                        color = accentColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Randevu bilgileri yükleniyor...",
                        style = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (state.physiotherapist != null)
                                    "Fizyoterapist: ${state.physiotherapist?.firstName} ${state.physiotherapist?.lastName}"
                                else
                                    "Fizyoterapist bilgileri yükleniyor...",
                                style = LocalTextStyle.current.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = primaryColor
                                )
                            )
                            if (state.physiotherapist != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Konum: ${state.physiotherapist?.city} / ${state.physiotherapist?.district}",
                                    style = LocalTextStyle.current.copy(
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                )
                            }
                        }
                    }
                    var selectedMonth by remember { mutableStateOf(0) }
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
                                    if (selectedMonth < 3) selectedMonth++
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
                            viewModel.onEvent(AppointmentBookingEvent.DateSelected(date))
                        },
                        selectedDate = state.selectedDate
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Randevu Tipi",
                        style = LocalTextStyle.current.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = primaryColor
                        ),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppointmentTypeButton(
                            text = "Yüz Yüze Randevu",
                            icon = Icons.Default.Person,
                            isSelected = state.selectedAppointmentType == AppointmentType.IN_PERSON,
                            onClick = {
                                viewModel.onEvent(AppointmentBookingEvent.AppointmentTypeSelected(AppointmentType.IN_PERSON))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AppointmentTypeButton(
                            text = "Uzaktan Randevu",
                            icon = Icons.Default.Videocam,
                            isSelected = state.selectedAppointmentType == AppointmentType.REMOTE,
                            onClick = {
                                viewModel.onEvent(AppointmentBookingEvent.AppointmentTypeSelected(AppointmentType.REMOTE))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (state.selectedDate != null) {
                        Text(
                            text = "Saat Seçin",
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
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
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
                                        tint = Color(0xFFFF9800)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Seçilen tarihte müsait saat bulunmamaktadır. Lütfen başka bir tarih seçiniz.",
                                        style = LocalTextStyle.current.copy(
                                            color = Color(0xFF795548),
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                            }
                        } else {
                            TimeSlotGrid(
                                timeSlots = state.availableTimeSlots,
                                selectedTimeSlot = state.selectedTimeSlot,
                                onTimeSlotSelected = { timeSlot ->
                                    viewModel.onEvent(AppointmentBookingEvent.TimeSlotSelected(timeSlot))
                                }
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
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
                                    tint = Color(0xFF2196F3)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Lütfen önce bir tarih seçiniz.",
                                    style = LocalTextStyle.current.copy(
                                        color = Color(0xFF0D47A1)
                                    )
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.onEvent(AppointmentBookingEvent.BookAppointment) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = state.selectedDate != null && state.selectedTimeSlot != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accentColor,
                            disabledContainerColor = Color.LightGray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Randevu Oluştur",
                            style = LocalTextStyle.current.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { }) {
                            Text(
                                text = "Tamam",
                                style = LocalTextStyle.current.copy(color = Color.White)
                            )
                        }
                    },
                    containerColor = Color(0xFFB71C1C)
                ) {
                    Text(
                        text = state.error!!,
                        style = LocalTextStyle.current.copy(color = Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarView(
    currentMonth: Int,
    onDateSelected: (Date) -> Unit,
    selectedDate: Date?
) {
    val primaryColor = Color(0xFF3B3E68)
    val accentColor = Color(0xFF6D72C3)
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, currentMonth)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val weekDays = listOf("P", "P", "S", "Ç", "P", "C", "C")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    style = LocalTextStyle.current.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val totalDays = firstDayOfMonth + daysInMonth
        val totalWeeks = (totalDays + 6) / 7
        for (week in 0 until totalWeeks) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (day in 0..6) {
                    val dayOfMonth = week * 7 + day - firstDayOfMonth + 1
                    if (dayOfMonth in 1..daysInMonth) {
                        val dateCalendar = Calendar.getInstance()
                        dateCalendar.add(Calendar.MONTH, currentMonth)
                        dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        val isSelected = selectedDate?.let {
                            val selectedCal = Calendar.getInstance()
                            selectedCal.time = it
                            selectedCal.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                                    selectedCal.get(Calendar.MONTH) == dateCalendar.get(Calendar.MONTH) &&
                                    selectedCal.get(Calendar.DAY_OF_MONTH) == dateCalendar.get(Calendar.DAY_OF_MONTH)
                        } ?: false
                        val todayCal = Calendar.getInstance()
                        val isToday = todayCal.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                                todayCal.get(Calendar.MONTH) == dateCalendar.get(Calendar.MONTH) &&
                                todayCal.get(Calendar.DAY_OF_MONTH) == dateCalendar.get(Calendar.DAY_OF_MONTH)
                        val isPastDay = dateCalendar.before(todayCal) && !isToday
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> accentColor
                                        isToday -> accentColor.copy(alpha = 0.3f)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable(enabled = !isPastDay) {
                                    if (!isPastDay) {
                                        onDateSelected(dateCalendar.time)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayOfMonth.toString(),
                                style = LocalTextStyle.current.copy(
                                    color = when {
                                        isSelected -> Color.White
                                        isPastDay -> Color.LightGray
                                        else -> Color.Black
                                    },
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeSlotGrid(
    timeSlots: List<String>,
    selectedTimeSlot: String?,
    onTimeSlotSelected: (String) -> Unit
) {
    val accentColor = Color(0xFF6D72C3)
    val rows = timeSlots.chunked(4)

    Column(modifier = Modifier.fillMaxWidth()) {
        rows.forEach { rowSlots ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowSlots.forEach { timeSlot ->
                    val isSelected = timeSlot == selectedTimeSlot

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) accentColor else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) accentColor else Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onTimeSlotSelected(timeSlot) }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = timeSlot,
                            style = LocalTextStyle.current.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.DarkGray
                            )
                        )
                    }
                }

                repeat(4 - rowSlots.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AppointmentTypeButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(0xFF6D72C3)

    OutlinedCard(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accentColor else Color.LightGray
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.1f) else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) accentColor else Color.Gray,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                style = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    color = if (isSelected) accentColor else Color.Gray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            )
        }
    }
}