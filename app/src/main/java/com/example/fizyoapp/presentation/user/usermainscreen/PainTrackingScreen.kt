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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import com.example.fizyoapp.presentation.ui.bottomnavbar.BottomNavbarComponent

import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PainTrackingScreen(
    navController: NavController,
    viewModel: PainTrackingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val showDeleteConfirmationDialog = remember { mutableStateOf(false) }
    val recordToDelete = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = true) {
        viewModel.onEvent(PainTrackingEvent.RefreshData)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ağrı Takibi", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(59, 62, 104)
                )
            )
        },
        floatingActionButton = {
            if (!state.isAddingRecord) {
                FloatingActionButton(
                    onClick = { viewModel.onEvent(PainTrackingEvent.ToggleAddRecord) },
                    containerColor = Color(59, 62, 104),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ağrı Kaydı Ekle")
                }
            }
        },
        bottomBar = { BottomNavbarComponent(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .padding(bottom = 60.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center),
                    color = Color(59, 62, 104)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (!state.isAddingRecord) {
                        if (state.painRecords.isEmpty()) {
                            // Boş durum
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Henüz kaydedilmiş ağrı kaydınız bulunmuyor.\n" +
                                            "Ağrı kaydı eklemek için + butonuna tıklayın.",
                                    textAlign = TextAlign.Center,
                                    color = Color.Gray
                                )
                            }
                        } else {

                            LazyColumn(
                                modifier = Modifier.weight(1f)
                            ) {
                                items(state.painRecords) { record ->
                                    PainRecordItem(
                                        painRecord = record,
                                        onEditClick = {
                                            viewModel.onEvent(PainTrackingEvent.EditPainRecord(record))
                                        },
                                        onDeleteClick = {
                                            recordToDelete.value = record.id
                                            showDeleteConfirmationDialog.value = true
                                        }
                                    )
                                }
                            }
                        }
                    } else {

                        AddEditPainRecordForm(
                            state = state,
                            onEvent = viewModel::onEvent
                        )
                    }


                    if (state.error != null) {
                        Snackbar(
                            modifier = Modifier.padding(8.dp),
                            action = {
                                TextButton(onClick = { viewModel.onEvent(PainTrackingEvent.DismissError) }) {
                                    Text("Tamam", color = Color.White)
                                }
                            },
                            containerColor = Color(0xFFB71C1C)
                        ) {
                            Text(state.error!!, color = Color.White)
                        }
                    }
                }
            }


            if (showDeleteConfirmationDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmationDialog.value = false },
                    title = { Text("Ağrı Kaydını Sil") },
                    text = { Text("Bu ağrı kaydını silmek istediğinizden emin misiniz?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                recordToDelete.value?.let {
                                    viewModel.onEvent(PainTrackingEvent.DeletePainRecord(it))
                                }
                                showDeleteConfirmationDialog.value = false
                            }
                        ) {
                            Text("Sil", color = Color.Red)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteConfirmationDialog.value = false }
                        ) {
                            Text("İptal")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PainRecordItem(
    painRecord: PainRecord,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(painRecord.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
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
                    text = painRecord.location,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Şiddet:",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Row {
                    repeat(10) { index ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .padding(horizontal = 2.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < painRecord.intensity) {
                                        when (index) {
                                            0, 1, 2 -> Color(0xFF388E3C)
                                            3, 4, 5 -> Color(0xFFFFA000)
                                            else -> Color(0xFFE53935)
                                        }
                                    } else {
                                        Color.LightGray
                                    }
                                )
                        )
                    }
                }
                Text(
                    text = "${painRecord.intensity}/10",
                    fontWeight = FontWeight.Bold,
                    color = Color(59, 62, 104),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (painRecord.note?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = painRecord.note.toString(),
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Düzenle",
                        tint = Color(59, 62, 104)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = Color(0xFFB71C1C)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditPainRecordForm(
    state: PainTrackingState,
    onEvent: (PainTrackingEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (state.currentPainRecord != null) "Ağrı Kaydını Düzenle" else "Yeni Ağrı Kaydı",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onEvent(PainTrackingEvent.ToggleAddRecord) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat"
                )
            }
        }


        OutlinedTextField(
            value = state.painLocation,
            onValueChange = { onEvent(PainTrackingEvent.UpdatePainLocation(it)) },
            label = { Text("Ağrı Lokasyonu") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true
        )


        Text(
            text = "Ağrı Şiddeti: ${state.painIntensity}/10",
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Slider(
            value = state.painIntensity.toFloat(),
            onValueChange = { onEvent(PainTrackingEvent.UpdatePainIntensity(it.toInt())) },
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Hafif (0)", fontSize = 12.sp)
            Text("Orta (5)", fontSize = 12.sp)
            Text("Şiddetli (10)", fontSize = 12.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(10) { index ->
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(horizontal = 2.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < state.painIntensity) {
                                when (index) {
                                    0, 1, 2 -> Color(0xFF388E3C)
                                    3, 4, 5 -> Color(0xFFFFA000)
                                    else -> Color(0xFFE53935)
                                }
                            } else {
                                Color.LightGray
                            }
                        )
                        .clickable { onEvent(PainTrackingEvent.UpdatePainIntensity(index + 1)) }
                )
            }
        }


        OutlinedTextField(
            value = state.painDescription,
            onValueChange = { onEvent(PainTrackingEvent.UpdatePainDescription(it)) },
            label = { Text("Ağrı Açıklaması (İsteğe Bağlı)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(vertical = 8.dp),
            maxLines = 5
        )

        Button(
            onClick = { onEvent(PainTrackingEvent.SavePainRecord) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(59, 62, 104)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Kaydet",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}