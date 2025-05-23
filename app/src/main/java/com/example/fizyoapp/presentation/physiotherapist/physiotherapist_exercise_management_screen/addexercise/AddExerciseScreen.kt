package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.addexercise

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseScreen(
    navController: NavController,
    viewModel: AddExerciseViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Media viewer state
    var showMediaViewer by remember { mutableStateOf(false) }
    var selectedMediaUrl by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf("") }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(AddExerciseEvent.AddMedia(it.toString(), "image"))
        }
    }

    // Video picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(AddExerciseEvent.AddMedia(it.toString(), "video"))
        }
    }

    // Category selection dialog
    var showCategoryDialog by remember { mutableStateOf(false) }

    // Difficulty selection dialog
    var showDifficultyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AddExerciseViewModel.UiEvent.NavigateBack -> {
                    // popBackStack yerine yeni bir rota ile navigate kullanın
                    navController.navigate(AppScreens.ExerciseManagementScreen.route) {
                        popUpTo(AppScreens.ExerciseManagementScreen.route) {
                            inclusive = true
                        }
                    }
                }
                is AddExerciseViewModel.UiEvent.ShowError -> {
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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Yeni Egzersiz",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
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
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Title
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Title field
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = { viewModel.onEvent(AddExerciseEvent.TitleChanged(it)) },
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Category selection
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Kategori Seç",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                disabledTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description and Instructions
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Description
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.onEvent(
                                AddExerciseEvent.DescriptionChanged(
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
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Detailed instructions
                        OutlinedTextField(
                            value = state.instructions,
                            onValueChange = { viewModel.onEvent(
                                AddExerciseEvent.InstructionsChanged(
                                    it
                                )
                            ) },
                            label = { Text("Detaylı Talimatlar") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            maxLines = 10,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Zorluk Seviyesi
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Sadece zorluk seviyesi seçimini bırakalım
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
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Zorluk Seç",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                disabledBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                disabledTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Media section
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
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
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Egzersiz için görsel veya video ekleyin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Media buttons
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

                        // Preview of selected media
                        AnimatedVisibility(
                            visible = state.mediaUris.isNotEmpty(),
                            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                            exit = fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Seçilen Medyalar (${state.mediaUris.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    items(state.mediaUris) { uri ->
                                        MediaPreviewItem(
                                            uri = uri,
                                            onRemove = { viewModel.onEvent(AddExerciseEvent.RemoveMedia(uri)) },
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

                // Save button
                ElevatedButton(
                    onClick = { viewModel.onEvent(AddExerciseEvent.SaveExercise) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !state.isLoading && state.title.isNotBlank() && state.category.isNotBlank(),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
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
                            text = "Egzersizi Kaydet",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Category selection dialog
            if (showCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showCategoryDialog = false },
                    icon = { Icon(Icons.Default.Category, contentDescription = null) },
                    title = {
                        Text(
                            "Kategori Seç",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
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
                                            viewModel.onEvent(
                                                AddExerciseEvent.CategoryChanged(
                                                    category
                                                )
                                            )
                                            showCategoryDialog = false
                                        },
                                    color = if (category == state.category)
                                        MaterialTheme.colorScheme.primaryContainer
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
                                                viewModel.onEvent(
                                                    AddExerciseEvent.CategoryChanged(
                                                        category
                                                    )
                                                )
                                                showCategoryDialog = false
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Text(
                                            text = category,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (category == state.category)
                                                FontWeight.Bold
                                            else
                                                FontWeight.Normal,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showCategoryDialog = false }) {
                            Text("İptal")
                        }
                    }
                )
            }

            // Difficulty selection dialog
            if (showDifficultyDialog) {
                AlertDialog(
                    onDismissRequest = { showDifficultyDialog = false },
                    icon = { Icon(Icons.Outlined.FitnessCenter, contentDescription = null) },
                    title = {
                        Text(
                            "Zorluk Seviyesi Seç",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
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
                                    viewModel.onEvent(AddExerciseEvent.DifficultyChanged(ExerciseDifficulty.EASY))
                                    showDifficultyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DifficultyOption(
                                title = "Orta",
                                description = "Orta seviye egzersizler",
                                isSelected = state.difficulty == ExerciseDifficulty.MEDIUM,
                                onClick = {
                                    viewModel.onEvent(AddExerciseEvent.DifficultyChanged(ExerciseDifficulty.MEDIUM))
                                    showDifficultyDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DifficultyOption(
                                title = "Zor",
                                description = "İleri seviye egzersizler",
                                isSelected = state.difficulty == ExerciseDifficulty.HARD,
                                onClick = {
                                    viewModel.onEvent(AddExerciseEvent.DifficultyChanged(ExerciseDifficulty.HARD))
                                    showDifficultyDialog = false
                                }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDifficultyDialog = false }) {
                            Text("İptal")
                        }
                    }
                )
            }

            // Media viewer dialog
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
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
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

        // Gradient overlay for better icon visibility
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

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Kaldır",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(16.dp)
            )
        }

        // File type indicator
        if (uri.contains("video")) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
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
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp,
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
                    selectedColor = MaterialTheme.colorScheme.primary
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
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}