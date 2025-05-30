package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexerciseplan

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseType
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.DatePickerField
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.MediaViewer
import com.example.fizyoapp.presentation.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CreateExercisePlanViewModel.UiEvent.NavigateBack -> {
                    Log.d("CreateExercisePlanScreen", "Navigating back")
                    navController.popBackStack()
                }
                is CreateExercisePlanViewModel.UiEvent.ShowError -> {
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
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Egzersiz Planı Oluştur",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
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
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = surfaceColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Plan Bilgileri",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                                        color = errorColor
                                    )
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Title,
                                    contentDescription = null,
                                    tint = primaryColor
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                                cursorColor = primaryColor
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                                    contentDescription = null,
                                    tint = primaryColor
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Hasta Seç",
                                    tint = primaryColor
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledBorderColor = primaryColor.copy(alpha = 0.5f),
                                disabledLabelColor = textColor.copy(alpha = 0.7f),
                                disabledTextColor = textColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = surfaceColor
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Detaylar ve Tarihler",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.onEvent(CreateExercisePlanEvent.DescriptionChanged(it)) },
                            label = { Text("Açıklama") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = primaryColor
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                                cursorColor = primaryColor
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DatePickerField(
                                date = state.startDate,
                                onDateSelected = { viewModel.onEvent(CreateExercisePlanEvent.StartDateChanged(it)) },
                                label = "Başlangıç Tarihi",
                                modifier = Modifier.weight(1f)
                            )
                            DatePickerField(
                                date = state.endDate,
                                onDateSelected = { viewModel.onEvent(CreateExercisePlanEvent.EndDateChanged(it)) },
                                label = "Bitiş Tarihi",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.frequency,
                            onValueChange = { viewModel.onEvent(CreateExercisePlanEvent.FrequencyChanged(it)) },
                            label = { Text("Sıklık (örn. Günde 2 kez, Haftada 3 gün)") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = primaryColor
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                                cursorColor = primaryColor
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = { viewModel.onEvent(CreateExercisePlanEvent.NotesChanged(it)) },
                            label = { Text("Notlar") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Note,
                                    contentDescription = null,
                                    tint = primaryColor
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                                cursorColor = primaryColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = surfaceColor
                    ),
                    shape = RoundedCornerShape(16.dp)
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
                                text = "Egzersizler",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            )
                            Button(
                                onClick = { showExercisesDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
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

                        AnimatedVisibility(visible = state.exercises.isNotEmpty()) {
                            Column {
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
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = textColor.copy(alpha = 0.1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (!state.isLoading) {
                            viewModel.onEvent(CreateExercisePlanEvent.SavePlan)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading && state.title.isNotBlank() && state.selectedPatient != null && state.exercises.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White,
                        disabledContainerColor = primaryColor.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
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

            if (showPatientDialog) {
                AlertDialog(
                    onDismissRequest = { showPatientDialog = false },
                    title = {
                        Text(
                            "Hasta Seç",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (state.isLoadingPatients) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = primaryColor)
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
                                            imageVector = Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = primaryColor.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "Mesajlaştığınız hasta bulunamadı",
                                            style = MaterialTheme.typography.titleMedium,
                                            textAlign = TextAlign.Center,
                                            color = textColor
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Lütfen önce bir hastayla mesajlaşın veya hasta ekleyin",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = textColor.copy(alpha = 0.7f)
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
                        TextButton(
                            onClick = { showPatientDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)
                        ) {
                            Text("Kapat")
                        }
                    },
                    containerColor = surfaceColor
                )
            }

            if (showExercisesDialog) {
                AlertDialog(
                    onDismissRequest = { showExercisesDialog = false },
                    title = {
                        Text(
                            "Egzersiz Seç",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    },
                    text = {
                        if (state.isLoadingExercises) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = primaryColor)
                            }
                        } else if (state.availableExercises.isEmpty()) {
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
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = primaryColor.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Egzersiz bulunamadı",
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center,
                                        color = textColor
                                    )
                                }
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
                        TextButton(
                            onClick = { showExercisesDialog = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)
                        ) {
                            Text("İptal")
                        }
                    },
                    containerColor = surfaceColor
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
        color = if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.1f))
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
                        tint = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = patient.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) primaryColor else textColor
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seçildi",
                    tint = primaryColor
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        color = if (isSelected) primaryColor.copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (exercise.mediaUrls.isNotEmpty()) {
                val firstMediaUrl = exercise.mediaUrls.first()
                MediaPreviewItem(
                    uri = firstMediaUrl,
                    mediaTypes = exercise.mediaType,
                    onClick = onClick,
                    size = 48.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) primaryColor else textColor
                )
                Text(
                    text = exercise.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seçildi",
                    tint = primaryColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            if (exercise.mediaUrls.isNotEmpty()) {
                val firstMediaUrl = exercise.mediaUrls.first()
                MediaPreviewItem(
                    uri = firstMediaUrl,
                    mediaTypes = exercise.mediaTypes,
                    onClick = {
                        selectedMediaUrl = firstMediaUrl
                        showMediaViewer = true
                    },
                    size = 48.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = exercise.exerciseTitle,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                color = textColor
            )

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Daralt" else "Genişlet",
                    tint = primaryColor
                )
            }

            IconButton(onClick = onRemoveClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Kaldır",
                    tint = errorColor
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                if (exercise.mediaUrls.isNotEmpty()) {
                    val firstMediaUrl = exercise.mediaUrls.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable {
                                selectedMediaUrl = firstMediaUrl
                                showMediaViewer = true
                            },
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(firstMediaUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = exercise.exerciseTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            if (exercise.mediaTypes[firstMediaUrl] == ExerciseType.VIDEO ||
                                firstMediaUrl.contains("video") ||
                                firstMediaUrl.contains(".mp4") ||
                                firstMediaUrl.contains(".mov") ||
                                firstMediaUrl.contains(".avi") ||
                                firstMediaUrl.contains(".webm")) {

                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(overlayColor, CircleShape)
                                            .padding(8.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                        .background(
                                            color = overlayColor,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Video",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                            cursorColor = primaryColor
                        )
                    )

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
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                            cursorColor = primaryColor
                        )
                    )

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
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                            cursorColor = primaryColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = {
                        notes = it
                        onDetailsChanged(sets, repetitions, duration, notes)
                    },
                    label = { Text("Notlar") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                        cursorColor = primaryColor
                    )
                )
            }
        }

        if (showMediaViewer && selectedMediaUrl.isNotEmpty()) {
            val mediaType = if (exercise.mediaTypes.containsKey(selectedMediaUrl)) {
                if (exercise.mediaTypes[selectedMediaUrl] == ExerciseType.VIDEO) "video" else "image"
            } else {
                if (selectedMediaUrl.contains("video") ||
                    selectedMediaUrl.contains(".mp4") ||
                    selectedMediaUrl.contains(".mov") ||
                    selectedMediaUrl.contains(".avi") ||
                    selectedMediaUrl.contains(".webm")) "video" else "image"
            }
            MediaViewer(
                mediaUrl = selectedMediaUrl,
                mediaType = mediaType,
                onDismiss = { showMediaViewer = false }
            )
        }
    }
}

@Composable
fun MediaPreviewItem(
    uri: String,
    mediaTypes: Map<String, ExerciseType> = emptyMap(),
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 110.dp,
    onRemove: (() -> Unit)? = null
) {
    val isVideo = mediaTypes[uri] == ExerciseType.VIDEO ||
            uri.contains("video") || uri.contains(".mp4") ||
            uri.contains(".mov") || uri.contains(".avi") ||
            uri.contains(".webm")

    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Black.copy(alpha = 0.3f)
                        ),
                        startY = 0f,
                        endY = size.value
                    )
                )
        )

        onRemove?.let {
            IconButton(
                onClick = it,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .background(
                        color = errorColor.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kaldır",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        if (isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(size / 3)
                    .background(
                        color = overlayColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Video",
                    tint = Color.White,
                    modifier = Modifier.size(size / 5)
                )
            }

            if (size >= 80.dp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .background(
                            color = overlayColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Video",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}