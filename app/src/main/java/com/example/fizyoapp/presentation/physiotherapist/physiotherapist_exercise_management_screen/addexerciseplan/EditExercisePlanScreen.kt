package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexerciseplan


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlanItem
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlanStatus
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.DatePickerField
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.MediaViewer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExercisePlanScreen(
    navController: NavController,
    viewModel: EditExercisePlanViewModel = hiltViewModel(),
    planId: String
) {
    val state = viewModel.state.collectAsState().value
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Load plan data when screen is created
    LaunchedEffect(key1 = planId) {
        viewModel.loadExercisePlan(planId)
    }

    // Handle UI events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is EditExercisePlanViewModel.UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
                is EditExercisePlanViewModel.UiEvent.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                is EditExercisePlanViewModel.UiEvent.ShowSuccess -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Egzersiz Planını Düzenle") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Plan durumu değiştirme butonu
                    if (!state.isLoading && state.plan != null) {
                        IconButton(onClick = { viewModel.toggleStatusSelectionDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Durum Değiştir",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        // Status değiştirme dialog'u
        if (state.showStatusSelectionDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleStatusSelectionDialog() },
                title = { Text("Plan Durumunu Değiştir") },
                text = {
                    Column {
                        RadioButton(
                            selected = state.selectedStatus == ExercisePlanStatus.ACTIVE,
                            onClick = { viewModel.onStatusSelected(ExercisePlanStatus.ACTIVE) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Aktif", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(8.dp))

                        RadioButton(
                            selected = state.selectedStatus == ExercisePlanStatus.COMPLETED,
                            onClick = { viewModel.onStatusSelected(ExercisePlanStatus.COMPLETED) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("Tamamlandı", style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(8.dp))

                        RadioButton(
                            selected = state.selectedStatus == ExercisePlanStatus.CANCELLED,
                            onClick = { viewModel.onStatusSelected(ExercisePlanStatus.CANCELLED) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("İptal Edildi", style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updatePlanStatus()
                            viewModel.toggleStatusSelectionDialog()
                        }
                    ) {
                        Text("Kaydet")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.toggleStatusSelectionDialog() }) {
                        Text("İptal")
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadExercisePlan(planId) }) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            } else if (state.plan != null) {
                // Plan detayları formu
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Başlık
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = { viewModel.onTitleChanged(it) },
                            label = { Text("Plan Adı") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Title,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    item {
                        // Hasta bilgisi (salt okunur)
                        OutlinedTextField(
                            value = state.patientName,
                            onValueChange = { },
                            label = { Text("Hasta") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    item {
                        // Açıklama
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.onDescriptionChanged(it) },
                            label = { Text("Açıklama") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    item {
                        // Tarih aralığı
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Başlangıç tarihi
                            DatePickerField(
                                date = state.startDate,
                                onDateSelected = { viewModel.onStartDateChanged(it) },
                                label = "Başlangıç Tarihi",
                                modifier = Modifier.weight(1f)
                            )

                            // Bitiş tarihi
                            DatePickerField(
                                date = state.endDate,
                                onDateSelected = { viewModel.onEndDateChanged(it) },
                                label = "Bitiş Tarihi",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        // Sıklık
                        OutlinedTextField(
                            value = state.frequency,
                            onValueChange = { viewModel.onFrequencyChanged(it) },
                            label = { Text("Sıklık (örn. Günde 2 kez)") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    item {
                        // Notlar
                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = { viewModel.onNotesChanged(it) },
                            label = { Text("Notlar") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Note,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    item {
                        // Egzersizler başlık
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Egzersizler",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Egzersiz ekleme düğmesini burada ekleyebilirsin
                            // Şimdilik sadece var olanları düzenliyoruz
                        }
                    }

                    // Egzersiz listesi
                    items(state.exercises) { exerciseItem ->
                        ExerciseItemCard(
                            exerciseItem = exerciseItem,
                            onDetailsChanged = { sets, reps, duration, notes ->
                                viewModel.updateExerciseDetails(
                                    exerciseItem.exerciseId,
                                    sets,
                                    reps,
                                    duration,
                                    notes
                                )
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Kaydet düğmesi
                        Button(
                            onClick = { viewModel.saveExercisePlan() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Planı Güncelle")
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItemCard(
    exerciseItem: ExercisePlanItem,
    onDetailsChanged: (sets: String, reps: String, duration: String, notes: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var sets by remember { mutableStateOf(exerciseItem.sets.toString()) }
    var repetitions by remember { mutableStateOf(exerciseItem.repetitions.toString()) }
    var duration by remember { mutableStateOf(exerciseItem.duration.toString()) }
    var notes by remember { mutableStateOf(exerciseItem.notes) }
    var showMediaViewer by remember { mutableStateOf(false) }
    var selectedMediaUrl by remember { mutableStateOf("") }

    LaunchedEffect(exerciseItem) {
        sets = exerciseItem.sets.toString()
        repetitions = exerciseItem.repetitions.toString()
        duration = exerciseItem.duration.toString()
        notes = exerciseItem.notes
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                // Thumbnail gösterimi (varsa)
                if (exerciseItem.mediaUrls.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedMediaUrl = exerciseItem.mediaUrls.first()
                                showMediaViewer = true
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(exerciseItem.mediaUrls.first())
                                .crossfade(true)
                                .build(),
                            contentDescription = exerciseItem.exerciseTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Egzersiz başlığı
                Text(
                    text = exerciseItem.exerciseTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Genişlet/daralt butonu
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Daralt" else "Genişlet"
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Eğer medya varsa önizleme göster
                    if (exerciseItem.mediaUrls.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    selectedMediaUrl = exerciseItem.mediaUrls.first()
                                    showMediaViewer = true
                                }
                        ) {
                            AsyncImage(
                                model = exerciseItem.mediaUrls.first(),
                                contentDescription = exerciseItem.exerciseTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Egzersiz parametreleri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sets
                        OutlinedTextField(
                            value = sets,
                            onValueChange = {
                                sets = it.filter { char -> char.isDigit() }
                                onDetailsChanged(sets, repetitions, duration, notes)
                            },
                            label = { Text("Set") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            )
                        )

                        // Repetitions
                        OutlinedTextField(
                            value = repetitions,
                            onValueChange = {
                                repetitions = it.filter { char -> char.isDigit() }
                                onDetailsChanged(sets, repetitions, duration, notes)
                            },
                            label = { Text("Tekrar") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            )
                        )

                        // Duration
                        OutlinedTextField(
                            value = duration,
                            onValueChange = {
                                duration = it.filter { char -> char.isDigit() }
                                onDetailsChanged(sets, repetitions, duration, notes)
                            },
                            label = { Text("Süre (sn)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = {
                            notes = it
                            onDetailsChanged(sets, repetitions, duration, notes)
                        },
                        label = { Text("Notlar") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )
                }
            }

            // Medya görüntüleyici
            if (showMediaViewer && selectedMediaUrl.isNotEmpty()) {
                MediaViewer(
                    mediaUrl = selectedMediaUrl,
                    mediaType = if (selectedMediaUrl.contains("video")) "video" else "image",
                    onDismiss = { showMediaViewer = false }
                )
            }
        }
    }
}