package com.example.fizyoapp.presentation.user.usermainscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import com.example.fizyoapp.domain.model.usermainscreen.Reminder
import com.example.fizyoapp.domain.model.usermainscreen.StepCount
import com.example.fizyoapp.domain.model.usermainscreen.WaterIntake
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.ui.bottomnavbar.BottomNavbarComponent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserMainScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    // UI event collection
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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val displayName = when {
                            state.userProfile?.firstName?.isNotEmpty() == true ->
                                "${state.userProfile.firstName} ${state.userProfile.lastName}".trim()
                            state.userName != null -> state.userName
                            else -> "Hasta"
                        }
                        Text("Merhaba, $displayName")
                        Text(
                            "İyi günler",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {

                    IconButton(onClick = { /* Bildirimler */ }) {
                        BadgedBox(badge = { Badge { Text("2") } }) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Bildirimler"
                            )
                        }
                    }

                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Ayarlar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(59, 62, 104),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavbarComponent(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(59, 62, 104))
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center),
                    color = Color.White
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        // Hatırlatmalar Kartı
                        RemindersCard(state.reminders)

                        // Ana Butonlar
                        MainNavigationButtons(navController)

                        // Su Tüketimi Kartı
                        WaterIntakeCard(
                            waterIntake = state.waterIntake,
                            onWaterChange = { glasses -> viewModel.onEvent(UserEvent.UpdateWaterIntake(glasses)) }
                        )

                        // Adım Sayacı Kartı
                        StepCounterCard(
                            stepCount = state.stepCount,
                            onStepUpdate = { steps -> viewModel.onEvent(UserEvent.UpdateStepCount(steps)) }
                        )

                        // Ağrı Takibi
                        PainSymptomSummaryCard(navController, state.latestPainRecord)
                    }

                    // Ekranda padding ekleyerek bottom bar'ın üzerine içerik gelmesini engelliyoruz
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }

            // Hata gösterim
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.onEvent(UserEvent.DismissError) }) {
                            Text("Tamam", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFFB71C1C)
                ) {
                    Text(state.error, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun MainNavigationButtons(navController: NavController) {
    // Egzersizlerim Butonu
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .height(130.dp),
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
                Modifier
                    .padding(start = 17.dp)
                    .size(40.dp)
            )
        }
    }
    // Orta Butonlar
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = { navController.navigate(AppScreens.OrnekEgzersizler.route) },
            modifier = Modifier
                .height(130.dp)
                .weight(1f),
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessibilityNew,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Örnek Egzersizler",
                    style = TextStyle(fontSize = 17.sp),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
        }
        Button(
            onClick = { navController.navigate(AppScreens.HastaliklarimScreen.route) },
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.DarkGray,
                containerColor = Color.White
            ),
            modifier = Modifier
                .height(130.dp)
                .weight(1f),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 6.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Healing,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hastalıklarım",
                    fontStyle = FontStyle.Italic,
                    style = TextStyle(fontSize = 17.sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    // Rehabilitasyon Geçmişim Butonu
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = {},
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
                .padding(vertical = 10.dp)
                .height(130.dp),
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

@Composable
fun RemindersCard(reminders: List<Reminder>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp, top = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
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
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Color(59, 62, 104),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Hatırlatmalar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (reminders.isEmpty()) {
                // Varsayılan hatırlatmalar göster
                DefaultReminders()
            } else {
                // Gerçek hatırlatmalar
                reminders.forEach { reminder ->
                    ReminderItem(reminder)
                }
            }
        }
    }
}

@Composable
fun DefaultReminders() {
    // Hatırlatma 1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(59, 62, 104))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Günlük egzersizlerinizi yapmayı unutmayın!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }

    // Hatırlatma 2
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(59, 62, 104))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Günde en az 30 dakika yürüyüş yapmalısınız",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }

    // Hatırlatma 3
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(59, 62, 104))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "Yarın saat 14:30'da randevunuz var",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }
}

@Composable
fun ReminderItem(reminder: Reminder) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(59, 62, 104))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = reminder.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )
    }
    if (reminder.description.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reminder.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }

    // Tarih formatter
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateFormat.format(Date(reminder.timestamp)),
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = Color(59, 62, 104)
        )
    }
}

@Composable
fun WaterIntakeCard(waterIntake: WaterIntake, onWaterChange: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
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
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Günlük Su Tüketimi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Su bardakları
                Row {
                    repeat(8) { index ->
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = if (index < waterIntake.glasses) Color(0xFF2196F3) else Color.LightGray,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(horizontal = 2.dp)
                                .clickable { onWaterChange(index + 1) }
                        )
                    }
                }

                // Sayaç ve hedef
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${waterIntake.glasses}/8 bardak",
                        fontWeight = FontWeight.Bold,
                        color = Color(59, 62, 104)
                    )
                    Text(
                        text = "${waterIntake.milliliters} ml / 1600 ml",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = waterIntake.glasses / 8f,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2196F3),
                trackColor = Color.LightGray
            )
        }
    }
}

@Composable
fun StepCounterCard(stepCount: StepCount, onStepUpdate: (Int) -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(59, 62, 104).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = Color(59, 62, 104)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Günlük Adım",
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${stepCount.steps}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(59, 62, 104)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "/ 8000 adım",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                LinearProgressIndicator(
                    progress = stepCount.steps / 8000f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = Color(59, 62, 104),
                    trackColor = Color.LightGray
                )
            }
        }
    }
}
@Composable
fun PainSymptomSummaryCard(navController: NavController, painRecord: PainRecord?) {
    val intensity = painRecord?.intensity ?: 0
    val location = painRecord?.location ?: "Veri yok"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { navController.navigate("pain_tracking") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ağrı Takibi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = if (painRecord != null) {
                        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        dateFormat.format(Date(painRecord.timestamp))
                    } else {
                        "Bugün"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Ağrı seviyesi göstergesi
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Şiddet:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    repeat(10) { index ->
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(horizontal = 2.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < intensity) {
                                        when (index) {
                                            0, 1, 2 -> Color(0xFF388E3C) // Yeşil
                                            3, 4, 5 -> Color(0xFFFFA000) // Sarı/Turuncu
                                            else -> Color(0xFFE53935) // Kırmızı
                                        }
                                    } else {
                                        Color.LightGray
                                    }
                                )
                        )
                    }
                }
                Text(
                    text = "$intensity/10",
                    fontWeight = FontWeight.Bold,
                    color = Color(59, 62, 104),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Lokasyon: $location",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
                TextButton(
                    onClick = { navController.navigate("add_pain_record") },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("Yeni Kayıt", color = Color(59, 62, 104))
                }
            }
        }
    }
}