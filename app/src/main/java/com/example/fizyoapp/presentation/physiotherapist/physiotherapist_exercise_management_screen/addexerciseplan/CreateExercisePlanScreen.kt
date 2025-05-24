// presentation/physiotherapist/exercise/CreateExercisePlanScreen.kt
package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexerciseplan

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.fizyoapp.data.repository.exercisemanagescreen.PatientListItem
import com.example.fizyoapp.domain.model.exercisemanagescreen.Exercise
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlanItem
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.DatePickerField
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.MediaViewer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExercisePlanScreen(
    navController: NavController,
    viewModel: CreateExercisePlanViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    var showPatientDialog by remember { mutableStateOf(false) }
    var showExercisesDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    // DateFormat için
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CreateExercisePlanViewModel.UiEvent.NavigateBack -> {
                    Log.d("CreateExercisePlanScreen", "Navigating back")
                    navController.popBackStack()
                }
                is CreateExercisePlanViewModel.UiEvent.ShowError -> {
                    // Show a snackbar instead of a Toast
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = "Tamam",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Egzersiz Planı Oluştur") },
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
                    .verticalScroll(rememberScrollState())
            ) {
                // Başlık
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.onEvent(CreateExercisePlanEvent.TitleChanged(it)) },
                    label = { Text("Plan Adı") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = state.titleError != null,
                    supportingText = {
                        state.titleError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Title,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Hasta seçimi - MedicalReportScreen'deki gibi yapılandırılmış
                OutlinedTextField(
                    value = state.selectedPatient?.fullName ?: "",
                    onValueChange = { },
                    label = { Text("Hasta") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPatientDialog = true },
                    enabled = false,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Hasta Seç"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Açıklama
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.onEvent(
                        CreateExercisePlanEvent.DescriptionChanged(
                            it
                        )
                    ) },
                    label = { Text("Açıklama") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tarih aralığı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Başlangıç tarihi
                    DatePickerField(
                        date = state.startDate,
                        onDateSelected = { viewModel.onEvent(CreateExercisePlanEvent.StartDateChanged(it)) },
                        label = "Başlangıç Tarihi",
                        modifier = Modifier.weight(1f)
                    )

                    // Bitiş tarihi
                    DatePickerField(
                        date = state.endDate,
                        onDateSelected = { viewModel.onEvent(CreateExercisePlanEvent.EndDateChanged(it)) },
                        label = "Bitiş Tarihi",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sıklık
                OutlinedTextField(
                    value = state.frequency,
                    onValueChange = { viewModel.onEvent(CreateExercisePlanEvent.FrequencyChanged(it)) },
                    label = { Text("Sıklık (örn. Günde 2 kez, Haftada 3 gün)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Notlar
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { viewModel.onEvent(CreateExercisePlanEvent.NotesChanged(it)) },
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

                Spacer(modifier = Modifier.height(24.dp))

                // Egzersizler bölümü
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Egzersizler",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = { showExercisesDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Egzersiz Ekle"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Egzersiz Ekle")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Egzersiz listesi
                AnimatedVisibility(visible = state.exercises.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            state.exercises.forEachIndexed { index, exercise ->
                                ExerciseListItem(
                                    exercise = exercise,
                                    onRemoveClick = {
                                        viewModel.onEvent(CreateExercisePlanEvent.RemoveExercise(index))
                                    },
                                    onDetailsChanged = { sets, reps, duration, notes ->
                                        viewModel.onEvent(
                                            CreateExercisePlanEvent.UpdateExerciseDetails(
                                                index, sets, reps, duration, notes
                                            )
                                        )
                                    }
                                )
                                if (index < state.exercises.size - 1) {
                                    Divider(
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ElevatedButton(
                    onClick = {
                        Log.d("CreateExercisePlanScreen", "Save button clicked, isLoading: ${state.isLoading}")
                        if (!state.isLoading) {
                            viewModel.onEvent(CreateExercisePlanEvent.SavePlan)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading && state.title.isNotBlank() && state.selectedPatient != null && state.exercises.isNotEmpty()
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Planı Kaydet",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Hasta seçim diyaloğu - MedicalReportScreen'deki ShareDialog mantığıyla
            if (showPatientDialog) {
                AlertDialog(
                    onDismissRequest = { showPatientDialog = false },
                    title = {
                        Text(
                            "Hasta Seç",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Debug bilgileri (geliştirme sırasında)
                            if (state.isLoadingPatients) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else if (state.patients.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Chat,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Mesajlaştığınız hasta bulunamadı",
                                            style = MaterialTheme.typography.titleMedium,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Lütfen önce bir hastayla mesajlaşın veya hasta ekleyin",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                ) {
                                    items(state.patients) { patient ->
                                        PatientListItem(
                                            patient = patient,
                                            isSelected = state.selectedPatient?.userId == patient.userId,
                                            onClick = {
                                                viewModel.onEvent(CreateExercisePlanEvent.PatientSelected(patient))
                                                showPatientDialog = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showPatientDialog = false }) {
                            Text("Kapat")
                        }
                    }
                )
            }

            // Egzersiz seçim diyaloğu
            if (showExercisesDialog) {
                AlertDialog(
                    onDismissRequest = { showExercisesDialog = false },
                    title = { Text("Egzersiz Seç") },
                    text = {
                        if (state.isLoadingExercises) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (state.availableExercises.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Egzersiz bulunamadı")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                            ) {
                                items(state.availableExercises) { exercise ->
                                    ExerciseSelectionItem(
                                        exercise = exercise,
                                        isSelected = state.exercises.any { it.exerciseId == exercise.id },
                                        onClick = {
                                            viewModel.onEvent(CreateExercisePlanEvent.AddExercise(exercise))
                                            showExercisesDialog = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showExercisesDialog = false }) {
                            Text("İptal")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PatientListItem(
    patient: PatientListItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profil fotoğrafı
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (patient.profilePhotoUrl != null && patient.profilePhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(patient.profilePhotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = patient.fullName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Hasta adı
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = patient.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Seçim göstergesi
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seçildi",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ExerciseSelectionItem(
    exercise: Exercise,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Exercise thumbnail
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (exercise.mediaUrls.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(exercise.mediaUrls.first())
                        .crossfade(true)
                        .build(),
                    contentDescription = exercise.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Exercise details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = exercise.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = exercise.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Seçildi",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ExerciseListItem(
    exercise: ExercisePlanItem,
    onRemoveClick: () -> Unit,
    onDetailsChanged: (sets: String, reps: String, duration: String, notes: String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var sets by remember { mutableStateOf(exercise.sets.toString()) }
    var repetitions by remember { mutableStateOf(exercise.repetitions.toString()) }
    var duration by remember { mutableStateOf(exercise.duration.toString()) }
    var notes by remember { mutableStateOf(exercise.notes) }
    var showMediaViewer by remember { mutableStateOf(false) }
    var selectedMediaUrl by remember { mutableStateOf("") }

    LaunchedEffect(exercise) {
        sets = exercise.sets.toString()
        repetitions = exercise.repetitions.toString()
        duration = exercise.duration.toString()
        notes = exercise.notes
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail gösterimi (varsa)
            if (exercise.mediaUrls.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            selectedMediaUrl = exercise.mediaUrls.first()
                            showMediaViewer = true
                        }
                ) {
                    AsyncImage(
                        model = exercise.mediaUrls.first(),
                        contentDescription = exercise.exerciseTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Egzersiz başlığı
            Text(
                text = exercise.exerciseTitle,
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

            // Kaldır butonu
            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Kaldır",
                    tint = MaterialTheme.colorScheme.error
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
                if (exercise.mediaUrls.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                selectedMediaUrl = exercise.mediaUrls.first()
                                showMediaViewer = true
                            }
                    ) {
                        AsyncImage(
                            model = exercise.mediaUrls.first(),
                            contentDescription = exercise.exerciseTitle,
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