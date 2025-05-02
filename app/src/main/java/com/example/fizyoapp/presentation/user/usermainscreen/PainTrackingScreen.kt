package com.example.fizyoapp.presentation.user.usermainscreen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainTrackingScreen(
    navController: NavController,
    viewModel: PainTrackingViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ağrı Takibi") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(59, 62, 104),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_pain_record") },
                containerColor = Color(59, 62, 104),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ağrı Kaydı Ekle")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(59, 62, 104)
                )
            } else if (state.painRecords.isEmpty()) {
                // Kayıt yoksa
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Henüz ağrı kaydınız bulunmamaktadır.",
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("add_pain_record") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(59, 62, 104)
                        )
                    ) {
                        Text("Ağrı Kaydı Ekle")
                    }
                }
            } else {
                // Kayıtları listele
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(state.painRecords) { painRecord ->
                        PainRecordItem(painRecord = painRecord)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Hata gösterimi
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.onEvent(PainTrackingEvent.DismissError) }) {
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
fun PainRecordItem(painRecord: PainRecord) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(painRecord.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    text = "Ağrı Şiddeti: ${painRecord.intensity}/10",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Ağrı göstergesi
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(10) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .padding(horizontal = 2.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < painRecord.intensity) {
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lokasyon: ${painRecord.location}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (!painRecord.note.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Not: ${painRecord.note}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}