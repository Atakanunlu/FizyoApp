package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexerciseplan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlanItem
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlanStatus
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseType
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.DatePickerField
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.MediaViewer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val accentColor = Color(59, 62, 104)
private val textColor = Color.DarkGray

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
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = planId) {
        viewModel.loadExercisePlan(planId)
    }

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
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Egzersiz Planını Düzenle",
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
                ),
                actions = {
                    if (!state.isLoading && state.plan != null) {
                        IconButton(onClick = { viewModel.toggleStatusSelectionDialog() }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Durum Değiştir",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.showStatusSelectionDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleStatusSelectionDialog() },
                title = {
                    Text(
                        "Plan Durumunu Değiştir",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                },
                text = {
                    Column {
                        StatusOption(
                            title = "Aktif",
                            description = "Plan şu anda aktif olarak uygulanıyor",
                            isSelected = state.selectedStatus == ExercisePlanStatus.ACTIVE,
                            onClick = { viewModel.onStatusSelected(ExercisePlanStatus.ACTIVE) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusOption(
                            title = "Tamamlandı",
                            description = "Plan başarıyla tamamlandı",
                            isSelected = state.selectedStatus == ExercisePlanStatus.COMPLETED,
                            onClick = { viewModel.onStatusSelected(ExercisePlanStatus.COMPLETED) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusOption(
                            title = "İptal Edildi",
                            description = "Plan iptal edildi veya durduruldu",
                            isSelected = state.selectedStatus == ExercisePlanStatus.CANCELLED,
                            onClick = { viewModel.onStatusSelected(ExercisePlanStatus.CANCELLED) }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updatePlanStatus()
                            viewModel.toggleStatusSelectionDialog()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Kaydet")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.toggleStatusSelectionDialog() }) {
                        Text("İptal", color = primaryColor)
                    }
                },
                containerColor = surfaceColor
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
                    CircularProgressIndicator(
                        color = primaryColor,
                        strokeWidth = 3.dp
                    )
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
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadExercisePlan(planId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            } else if (state.plan != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
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
                                onValueChange = { viewModel.onTitleChanged(it) },
                                label = { Text("Plan Adı") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Title,
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
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = state.patientName,
                                onValueChange = { },
                                label = { Text("Hasta") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
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
                                onValueChange = { viewModel.onDescriptionChanged(it) },
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
                                    onDateSelected = { viewModel.onStartDateChanged(it) },
                                    label = "Başlangıç Tarihi",
                                    modifier = Modifier.weight(1f)
                                )
                                DatePickerField(
                                    date = state.endDate,
                                    onDateSelected = { viewModel.onEndDateChanged(it) },
                                    label = "Bitiş Tarihi",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = state.frequency,
                                onValueChange = { viewModel.onFrequencyChanged(it) },
                                label = { Text("Sıklık (örn. Günde 2 kez)") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Repeat,
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
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Durum: ",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                val statusColor = when(state.selectedStatus) {
                                    ExercisePlanStatus.ACTIVE -> Color(0, 150, 136)
                                    ExercisePlanStatus.COMPLETED -> Color(76, 175, 80)
                                    ExercisePlanStatus.CANCELLED -> Color(244, 67, 54)
                                }
                                Surface(
                                    color = statusColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = when(state.selectedStatus) {
                                            ExercisePlanStatus.ACTIVE -> "Aktif"
                                            ExercisePlanStatus.COMPLETED -> "Tamamlandı"
                                            ExercisePlanStatus.CANCELLED -> "İptal Edildi"
                                        },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(
                                    onClick = { viewModel.toggleStatusSelectionDialog() },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = primaryColor
                                    )
                                ) {
                                    Text("Değiştir")
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = state.notes,
                                onValueChange = { viewModel.onNotesChanged(it) },
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
                            Text(
                                text = "Egzersizler",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (state.exercises.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Bu planda egzersiz bulunamadı",
                                        color = textColor.copy(alpha = 0.7f)
                                    )
                                }
                            } else {
                                state.exercises.forEach { exerciseItem ->
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
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.saveExercisePlan() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White,
                            disabledContainerColor = primaryColor.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Kaydediliyor...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Planı Güncelle",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun StatusOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected)
            primaryColor.copy(alpha = 0.1f)
        else
            surfaceColor,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = primaryColor
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected)
                        primaryColor
                    else
                        textColor
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected)
                        primaryColor.copy(alpha = 0.7f)
                    else
                        textColor.copy(alpha = 0.7f)
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = primaryColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
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
                if (exerciseItem.mediaUrls.isNotEmpty()) {
                    val firstMediaUrl = exerciseItem.mediaUrls.first()
                    EditScreenMediaPreviewItem(
                        uri = firstMediaUrl,
                        mediaTypes = exerciseItem.mediaTypes,
                        onClick = {
                            selectedMediaUrl = firstMediaUrl
                            showMediaViewer = true
                        },
                        size = 48.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = exerciseItem.exerciseTitle,
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
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    if (exerciseItem.mediaUrls.isNotEmpty()) {
                        val firstMediaUrl = exerciseItem.mediaUrls.first()
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
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(firstMediaUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = exerciseItem.exerciseTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                val isVideo = exerciseItem.mediaTypes[firstMediaUrl] == ExerciseType.VIDEO ||
                                        firstMediaUrl.contains("video") ||
                                        firstMediaUrl.contains(".mp4") ||
                                        firstMediaUrl.contains(".mov") ||
                                        firstMediaUrl.contains(".avi") ||
                                        firstMediaUrl.contains(".webm")

                                if (isVideo) {
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
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                                .padding(8.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                            .background(
                                                color = Color.Black.copy(alpha = 0.6f),
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
                val mediaType = if (exerciseItem.mediaTypes.containsKey(selectedMediaUrl)) {
                    if (exerciseItem.mediaTypes[selectedMediaUrl] == ExerciseType.VIDEO) "video" else "image"
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
}

@Composable
fun EditScreenMediaPreviewItem(
    uri: String,
    mediaTypes: Map<String, ExerciseType> = emptyMap(),
    onClick: () -> Unit,
    size: Dp = 110.dp,
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
                color = textColor.copy(alpha = 0.2f),
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
                        color = Color.Red.copy(alpha = 0.7f),
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
                        color = Color.Black.copy(alpha = 0.6f),
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
                            color = Color.Black.copy(alpha = 0.6f),
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