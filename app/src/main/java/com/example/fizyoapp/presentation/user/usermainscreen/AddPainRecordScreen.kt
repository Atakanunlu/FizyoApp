package com.example.fizyoapp.presentation.user.usermainscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPainRecordScreen(
    navController: NavController,
    viewModel: AddPainRecordViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    // UI event collection
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddPainRecordViewModel.UiEvent.RecordAdded -> {
                    navController.navigateUp()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ağrı Kaydı Ekle") },
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                // Ağrı şiddeti seçimi
                Text(
                    text = "Ağrı Şiddeti (1-10)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Ağrı şiddeti gösterge çubukları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(10) { index ->
                        val level = index + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .padding(horizontal = 2.dp)
                                .clip(CircleShape)
                                .background(
                                    if (level <= state.intensity) {
                                        when (level) {
                                            1, 2, 3 -> Color(0xFF388E3C) // Yeşil
                                            4, 5, 6 -> Color(0xFFFFA000) // Sarı/Turuncu
                                            else -> Color(0xFFE53935) // Kırmızı
                                        }
                                    } else {
                                        Color.LightGray
                                    }
                                )
                                .clickable(onClick = {
                                    viewModel.onEvent(AddPainRecordEvent.SetIntensity(level))
                                })
                        ) {
                            Text(
                                text = level.toString(),
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ağrı lokasyonu girişi
                Text(
                    text = "Ağrının Lokasyonu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.location,
                    onValueChange = { viewModel.onEvent(AddPainRecordEvent.SetLocation(it)) },
                    placeholder = { Text("Örn: Sol diz, Bel bölgesi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Not girişi
                Text(
                    text = "Notlar (İsteğe bağlı)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.note,
                    onValueChange = { viewModel.onEvent(AddPainRecordEvent.SetNote(it)) },
                    placeholder = { Text("Ağrı ile ilgili ek bilgiler...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Kaydet butonu
                Button(
                    onClick = { viewModel.onEvent(AddPainRecordEvent.SubmitRecord) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(59, 62, 104)
                    ),
                    enabled = !state.isSubmitting
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("KAYDET")
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
                        TextButton(onClick = { viewModel.onEvent(AddPainRecordEvent.DismissError) }) {
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