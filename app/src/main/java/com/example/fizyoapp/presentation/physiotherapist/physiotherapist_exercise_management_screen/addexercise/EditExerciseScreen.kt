package com.example.fizyoapp.presentation.physiotherapist.exercise

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fizyoapp.domain.model.exercise.DEFAULT_EXERCISE_CATEGORIES
import com.example.fizyoapp.domain.model.exercise.ExerciseDifficulty
import com.example.fizyoapp.presentation.navigation.AppScreens
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
fun EditExerciseScreen(
    navController: NavController,
    exerciseId: String,
    viewModel: EditExerciseViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(key1 = exerciseId) {
        viewModel.loadExercise(exerciseId)
    }

    var showMediaViewer by remember { mutableStateOf(false) }
    var selectedMediaUrl by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(EditExerciseEvent.AddMedia(it.toString(), "image"))
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(EditExerciseEvent.AddMedia(it.toString(), "video"))
        }
    }

    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDifficultyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is EditExerciseViewModel.UiEvent.NavigateBack -> {
                    navController.navigate(AppScreens.ExerciseManagementScreen.route) {
                        popUpTo(AppScreens.ExerciseManagementScreen.route) {
                            inclusive = true
                        }
                    }
                }
                is EditExerciseViewModel.UiEvent.ShowError -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = "Tamam",
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                is EditExerciseViewModel.UiEvent.ShowSuccess -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = "Tamam",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                else -> {}
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
                        "Egzersizi Düzenle",
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
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadExercise(exerciseId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState)
                ) {
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
                                text = "Temel Bilgiler",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = state.title,
                                onValueChange = { viewModel.onEvent(EditExerciseEvent.TitleChanged(it)) },
                                label = { Text("Egzersiz Adı") },
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
                                value = state.category,
                                onValueChange = { },
                                label = { Text("Kategori") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCategoryDialog = true },
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Category,
                                        contentDescription = null,
                                        tint = primaryColor
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Kategori Seç",
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
                                text = "Açıklama ve Talimatlar",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = state.description,
                                onValueChange = { viewModel.onEvent(EditExerciseEvent.DescriptionChanged(it)) },
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
                            OutlinedTextField(
                                value = state.instructions,
                                onValueChange = { viewModel.onEvent(EditExerciseEvent.InstructionsChanged(it)) },
                                label = { Text("Detaylı Talimatlar") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 5,
                                maxLines = 10,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Assignment,
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
                                text = "Zorluk Seviyesi",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = when (state.difficulty) {
                                    ExerciseDifficulty.EASY -> "Kolay"
                                    ExerciseDifficulty.MEDIUM -> "Orta"
                                    ExerciseDifficulty.HARD -> "Zor"
                                },
                                onValueChange = { },
                                label = { Text("Zorluk Seviyesi") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDifficultyDialog = true },
                                enabled = false,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = primaryColor
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Zorluk Seç",
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
                                text = "Medya İçeriği",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Egzersiz için görsel veya video ekleyin",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MediaButton(
                                    icon = Icons.Outlined.Image,
                                    text = "Fotoğraf",
                                    onClick = { imagePickerLauncher.launch("image/*") }
                                )
                                MediaButton(
                                    icon = Icons.Outlined.Videocam,
                                    text = "Video",
                                    onClick = { videoPickerLauncher.launch("video/*") }
                                )
                            }
                            AnimatedVisibility(
                                visible = state.mediaUris.isNotEmpty(),
                                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                                exit = fadeOut()
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Divider(color = textColor.copy(alpha = 0.1f))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Seçilen Medyalar (${state.mediaUris.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = primaryColor
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        contentPadding = PaddingValues(vertical = 8.dp)
                                    ) {
                                        items(state.mediaUris) { uri ->
                                            MediaPreviewItem(
                                                uri = uri,
                                                onRemove = { viewModel.onEvent(EditExerciseEvent.RemoveMedia(uri)) },
                                                onClick = {
                                                    selectedMediaUrl = uri
                                                    selectedMediaType = if (uri.contains("video")) "video" else "image"
                                                    showMediaViewer = true
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.onEvent(EditExerciseEvent.UpdateExercise) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.isLoading && state.title.isNotBlank() && state.category.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White,
                            disabledContainerColor = primaryColor.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Güncelleniyor...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Egzersizi Güncelle",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (showCategoryDialog) {
                    AlertDialog(
                        onDismissRequest = { showCategoryDialog = false },
                        icon = { Icon(Icons.Default.Category, contentDescription = null, tint = primaryColor) },
                        title = {
                            Text(
                                "Kategori Seç",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                DEFAULT_EXERCISE_CATEGORIES.forEach { category ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                viewModel.onEvent(EditExerciseEvent.CategoryChanged(category))
                                                showCategoryDialog = false
                                            },
                                        color = if (category == state.category)
                                            primaryColor.copy(alpha = 0.1f)
                                        else
                                            Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp, horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = category == state.category,
                                                onClick = {
                                                    viewModel.onEvent(EditExerciseEvent.CategoryChanged(category))
                                                    showCategoryDialog = false
                                                },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = primaryColor
                                                )
                                            )
                                            Text(
                                                text = category,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (category == state.category)
                                                    FontWeight.Bold
                                                else
                                                    FontWeight.Normal,
                                                modifier = Modifier.padding(start = 8.dp),
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showCategoryDialog = false }) {
                                Text("İptal", color = primaryColor)
                            }
                        },
                        containerColor = surfaceColor
                    )
                }

                if (showDifficultyDialog) {
                    AlertDialog(
                        onDismissRequest = { showDifficultyDialog = false },
                        icon = { Icon(Icons.Outlined.FitnessCenter, contentDescription = null, tint = primaryColor) },
                        title = {
                            Text(
                                "Zorluk Seviyesi Seç",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                DifficultyOption(
                                    title = "Kolay",
                                    description = "Başlangıç seviyesi egzersizler",
                                    isSelected = state.difficulty == ExerciseDifficulty.EASY,
                                    onClick = {
                                        viewModel.onEvent(EditExerciseEvent.DifficultyChanged(ExerciseDifficulty.EASY))
                                        showDifficultyDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                DifficultyOption(
                                    title = "Orta",
                                    description = "Orta seviye egzersizler",
                                    isSelected = state.difficulty == ExerciseDifficulty.MEDIUM,
                                    onClick = {
                                        viewModel.onEvent(EditExerciseEvent.DifficultyChanged(ExerciseDifficulty.MEDIUM))
                                        showDifficultyDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                DifficultyOption(
                                    title = "Zor",
                                    description = "İleri seviye egzersizler",
                                    isSelected = state.difficulty == ExerciseDifficulty.HARD,
                                    onClick = {
                                        viewModel.onEvent(EditExerciseEvent.DifficultyChanged(ExerciseDifficulty.HARD))
                                        showDifficultyDialog = false
                                    }
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showDifficultyDialog = false }) {
                                Text("İptal", color = primaryColor)
                            }
                        },
                        containerColor = surfaceColor
                    )
                }

                if (showMediaViewer && selectedMediaUrl.isNotEmpty()) {
                    MediaViewer(
                        mediaUrl = selectedMediaUrl,
                        mediaType = selectedMediaType,
                        onDismiss = { showMediaViewer = false }
                    )
                }
            }
        }
    }
}

@Composable
fun MediaButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(68.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape
                ),
            shape = CircleShape,
            color = primaryColor.copy(alpha = 0.1f),
            border = BorderStroke(
                width = 1.dp,
                color = primaryColor.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = primaryColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun MediaPreviewItem(
    uri: String,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(110.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
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
                        endY = 110f
                    )
                )
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(28.dp)
                .background(
                    color = Color.Red.copy(alpha = 0.7f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Kaldır",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        if (uri.contains("video")) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DifficultyOption(
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