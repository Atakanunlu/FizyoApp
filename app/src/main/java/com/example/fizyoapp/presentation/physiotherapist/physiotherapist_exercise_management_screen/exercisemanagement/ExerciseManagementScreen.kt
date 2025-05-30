package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.exercisemanagement
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fizyoapp.domain.model.exercisemanagescreen.DEFAULT_EXERCISE_CATEGORIES
import com.example.fizyoapp.domain.model.exercisemanagescreen.Exercise
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseDifficulty
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlan
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlanStatus
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseType
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseManagementScreen(
    navController: NavController,
    viewModel: ExerciseManagementViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val selectedTab = remember { mutableStateOf(0) }
    val tabs = listOf("Egzersizlerim", "Egzersiz Planları")
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val wasOnAddExerciseScreen = remember { mutableStateOf(false) }
    val wasOnCreatePlanScreen = remember { mutableStateOf(false) }
    val showDeletePlanDialog = remember { mutableStateOf<ExercisePlan?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (wasOnAddExerciseScreen.value) {
                    viewModel.onEvent(ExerciseManagementEvent.RefreshExercises)
                    wasOnAddExerciseScreen.value = false
                }
                if (wasOnCreatePlanScreen.value) {
                    viewModel.onEvent(ExerciseManagementEvent.LoadExercisePlans)
                    wasOnCreatePlanScreen.value = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is ExerciseManagementViewModel.UiEvent.NavigateToAddExercise -> {
                    wasOnAddExerciseScreen.value = true
                    navController.navigate(AppScreens.AddExerciseScreen.route)
                }
                is ExerciseManagementViewModel.UiEvent.NavigateToCreatePlan -> {
                    wasOnCreatePlanScreen.value = true
                    navController.navigate(AppScreens.CreateExercisePlanScreen.route)
                }
                is ExerciseManagementViewModel.UiEvent.NavigateToExerciseCategories -> {
                    navController.navigate(AppScreens.ExerciseCategoriesScreen.route)
                }
                else -> {}
            }
        }
    }

    showDeletePlanDialog.value?.let { plan ->
        AlertDialog(
            onDismissRequest = { showDeletePlanDialog.value = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = warningColor) },
            title = {
                Text(
                    "Egzersiz Planını Sil",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Text(
                    "\"${plan.title}\" planını silmek istediğinize emin misiniz? Bu işlem geri alınamaz.",
                    color = textColor.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(ExerciseManagementEvent.DeleteExercisePlan(plan.id))
                        showDeletePlanDialog.value = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = errorColor,
                        contentColor = Color.White
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeletePlanDialog.value = null },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    )
                ) {
                    Text("İptal")
                }
            },
            containerColor = surfaceColor
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Egzersiz Yönetimi",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    if (selectedTab.value == 0) {
                        viewModel.onEvent(ExerciseManagementEvent.AddExercise)
                    } else {
                        viewModel.onEvent(ExerciseManagementEvent.CreateExercisePlan)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Ekle"
                    )
                },
                text = {
                    Text(
                        if (selectedTab.value == 0) "Yeni Egzersiz" else "Yeni Plan"
                    )
                },
                containerColor = primaryColor,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedTab.value == 0) {
                CategoryChipsRow(
                    categories = DEFAULT_EXERCISE_CATEGORIES,
                    selectedCategory = state.selectedCategory,
                    selectedDifficulty = state.selectedDifficulty,
                    onCategoryClick = { category ->
                        viewModel.onEvent(ExerciseManagementEvent.FilterByCategory(category))
                    },
                    onDifficultySelected = { difficulty ->
                        viewModel.onEvent(ExerciseManagementEvent.FilterByDifficulty(difficulty))
                    }
                )
            }

            Surface(
                color = surfaceColor,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab.value,
                    containerColor = Color.Transparent,
                    contentColor = primaryColor,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.value]),
                            height = 3.dp,
                            color = primaryColor
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab.value == index,
                            onClick = { selectedTab.value = index },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = if (selectedTab.value == index) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = if (index == 0) Icons.Default.FitnessCenter else Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            selectedContentColor = primaryColor,
                            unselectedContentColor = textColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }

            state.errorMessage?.let {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = it,
                            color = errorColor,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (selectedTab.value == 0) {
                                    viewModel.onEvent(ExerciseManagementEvent.LoadExercises)
                                } else {
                                    viewModel.onEvent(ExerciseManagementEvent.LoadExercisePlans)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Yeniden Dene")
                        }
                    }
                }
            }

            if (!state.isLoading && state.errorMessage == null) {
                when (selectedTab.value) {
                    0 -> ExercisesTab(
                        exercises = if (state.selectedCategory.isEmpty() && state.selectedDifficulty == null)
                            state.exercises
                        else
                            state.filteredExercises,
                        onEditExercise = { exercise ->
                            try {
                                navController.navigate("edit_exercise_screen/${exercise.id}")
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Navigasyon hatası: ${e.message}")
                                }
                            }
                        },
                        onDeleteExercise = { exercise ->
                            viewModel.onEvent(ExerciseManagementEvent.DeleteExercise(exercise.id))
                        }
                    )
                    1 -> ExercisePlansTab(
                        plans = state.exercisePlans,
                        patientNames = state.patientNames,
                        onPlanClick = { plan ->
                            navController.navigate("edit_exercise_plan_screen/${plan.id}")
                        },
                        onDeletePlan = { plan ->
                            showDeletePlanDialog.value = plan
                        }
                    )
                }
            }

            if (state.actionSuccess != null) {
                LaunchedEffect(state.actionSuccess) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.actionSuccess,
                            actionLabel = "Tamam",
                            duration = SnackbarDuration.Short
                        )
                        viewModel.onEvent(ExerciseManagementEvent.ClearActionSuccess)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChipsRow(
    categories: List<String>,
    selectedCategory: String,
    selectedDifficulty: ExerciseDifficulty?,
    onCategoryClick: (String) -> Unit,
    onDifficultySelected: (ExerciseDifficulty?) -> Unit
) {
    Surface(
        color = surfaceColor,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Kategoriler",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                color = primaryColor
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory.isEmpty(),
                        onClick = { onCategoryClick("") },
                        label = { Text("Tümü", color = if (selectedCategory.isEmpty()) Color.White else textColor) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtre",
                                modifier = Modifier.size(18.dp),
                                tint = if (selectedCategory.isEmpty()) Color.White else primaryColor
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White
                        )
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { onCategoryClick(category) },
                        label = { Text(category, color = if (category == selectedCategory) Color.White else textColor) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = primaryColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Zorluk Seviyesi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
                color = primaryColor
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedDifficulty == null,
                    onClick = { onDifficultySelected(null) },
                    label = { Text("Tümü", color = if (selectedDifficulty == null) Color.White else textColor) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedDifficulty == null) Color.White else primaryColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedDifficulty == ExerciseDifficulty.EASY,
                    onClick = { onDifficultySelected(ExerciseDifficulty.EASY) },
                    label = { Text("Kolay", color = if (selectedDifficulty == ExerciseDifficulty.EASY) Color.White else textColor) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedDifficulty == ExerciseDifficulty.EASY) Color.White else easyColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = easyColor,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedDifficulty == ExerciseDifficulty.MEDIUM,
                    onClick = { onDifficultySelected(ExerciseDifficulty.MEDIUM) },
                    label = { Text("Orta", color = if (selectedDifficulty == ExerciseDifficulty.MEDIUM) Color.White else textColor) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedDifficulty == ExerciseDifficulty.MEDIUM) Color.White else mediumColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = mediumColor,
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    selected = selectedDifficulty == ExerciseDifficulty.HARD,
                    onClick = { onDifficultySelected(ExerciseDifficulty.HARD) },
                    label = { Text("Zor", color = if (selectedDifficulty == ExerciseDifficulty.HARD) Color.White else textColor) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (selectedDifficulty == ExerciseDifficulty.HARD) Color.White else hardColor
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = hardColor,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun ExercisesTab(
    exercises: List<Exercise>,
    onEditExercise: (Exercise) -> Unit,
    onDeleteExercise: (Exercise) -> Unit
) {
    if (exercises.isEmpty()) {
        EmptyState(
            message = "Henüz hiç egzersiz eklemediniz.",
            icon = Icons.Default.FitnessCenter
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(exercises) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onEditClick = { onEditExercise(it) },
                    onDeleteClick = { onDeleteExercise(it) }
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onEditClick: (Exercise) -> Unit,
    onDeleteClick: (Exercise) -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick(exercise) }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor,
            contentColor = textColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        primaryColor.copy(alpha = 0.1f)
                    )
            ) {
                if (exercise.mediaUrls.isNotEmpty()) {
                    val firstMediaUrl = exercise.mediaUrls.first()
                    val isVideo = exercise.mediaType[firstMediaUrl] == ExerciseType.VIDEO ||
                            firstMediaUrl.contains("video") ||
                            firstMediaUrl.contains(".mp4") ||
                            firstMediaUrl.contains(".mov") ||
                            firstMediaUrl.contains(".avi") ||
                            firstMediaUrl.contains(".webm")

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(exercise.mediaUrls.first())
                            .crossfade(true)
                            .build(),
                        contentDescription = exercise.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        overlayColor.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )

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
                                    .size(32.dp)
                                    .background(
                                        color = overlayColor.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .padding(4.dp)
                            )
                        }

                        Box (
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                                .background(
                                    color = overlayColor.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Video",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = textColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    modifier = Modifier.padding(vertical = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = primaryColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = exercise.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    val difficultyText = when (exercise.difficulty) {
                        ExerciseDifficulty.EASY -> "Kolay"
                        ExerciseDifficulty.MEDIUM -> "Orta"
                        ExerciseDifficulty.HARD -> "Zor"
                    }
                    val difficultyColor = when (exercise.difficulty) {
                        ExerciseDifficulty.EASY -> easyColor
                        ExerciseDifficulty.MEDIUM -> mediumColor
                        ExerciseDifficulty.HARD -> hardColor
                    }

                    ExerciseDetailChip(
                        icon = Icons.Default.Star,
                        text = difficultyText,
                        iconTint = difficultyColor
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(errorColor.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = errorColor
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = warningColor) },
            title = {
                Text(
                    "Egzersizi Sil",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                Text(
                    "\"${exercise.title}\" egzersizini silmek istediğinize emin misiniz? Bu işlem geri alınamaz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteClick(exercise)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = errorColor,
                        contentColor = Color.White
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    )
                ) {
                    Text("İptal")
                }
            },
            containerColor = surfaceColor
        )
    }
}

@Composable
fun ExerciseDetailChip(
    icon: ImageVector,
    text: String,
    iconTint: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = iconTint.copy(alpha = 0.1f),
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = iconTint
            )
        }
    }
}

@Composable
fun ExercisePlansTab(
    plans: List<ExercisePlan>,
    patientNames: Map<String, String>,
    onPlanClick: (ExercisePlan) -> Unit,
    onDeletePlan: (ExercisePlan) -> Unit
) {
    if (plans.isEmpty()) {
        EmptyState(
            message = "Henüz hiç egzersiz planı oluşturmadınız.",
            icon = Icons.AutoMirrored.Filled.Assignment
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(plans) { plan ->
                val patientName = patientNames[plan.patientId] ?: "Hasta adı yok"
                ExercisePlanCard(
                    plan = plan,
                    patientName = patientName,
                    onPlanClick = onPlanClick,
                    onDeleteClick = onDeletePlan
                )
            }
        }
    }
}

@Composable
fun ExercisePlanCard(
    plan: ExercisePlan,
    patientName: String,
    onPlanClick: (ExercisePlan) -> Unit,
    onDeleteClick: (ExercisePlan) -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    val currentDate = Date()
    val isExpired = plan.endDate?.before(currentDate) ?: false

    val (statusColor, statusText) = when {
        isExpired -> Pair(errorColor, "Pasif")
        plan.status == ExercisePlanStatus.ACTIVE -> Pair(primaryColor, "Aktif")
        plan.status == ExercisePlanStatus.COMPLETED -> Pair(easyColor, "Tamamlandı")
        plan.status == ExercisePlanStatus.CANCELLED -> Pair(errorColor, "İptal Edildi")
        else -> Pair(Color.Gray, plan.status.name)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onPlanClick(plan) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = textColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onDeleteClick(plan) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = errorColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (plan.description.isNotBlank()) {
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = textColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hasta: $patientName",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${plan.exercises.size} egzersiz",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = primaryColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                val dateText = if (plan.startDate != null && plan.endDate != null) {
                    "${dateFormat.format(plan.startDate)} - ${dateFormat.format(plan.endDate)}"
                } else {
                    "Tarih belirtilmedi"
                }
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
                if (plan.frequency.isNotBlank()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = primaryColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = plan.frequency,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    icon: ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = primaryColor.copy(alpha = 0.1f),
                modifier = Modifier.size(120.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = primaryColor.copy(alpha = 0.3f)
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize(),
                    tint = primaryColor.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = textColor.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Egzersizlerinizi buradan yönetebilirsiniz",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}