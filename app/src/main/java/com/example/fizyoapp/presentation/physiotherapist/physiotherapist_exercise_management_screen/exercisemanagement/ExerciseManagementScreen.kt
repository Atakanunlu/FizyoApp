// presentation/physiotherapist/exercise/ExerciseManagementScreen.kt
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
import com.example.fizyoapp.presentation.navigation.AppScreens
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
                    // Egzersiz ekleme ekranından döndük, listeyi yenileyelim
                    viewModel.onEvent(ExerciseManagementEvent.RefreshExercises)
                    wasOnAddExerciseScreen.value = false
                }
                if (wasOnCreatePlanScreen.value) {
                    // Plan oluşturma ekranından döndük, planları yenileyelim
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

    // Delete plan dialog outside the scaffold
    showDeletePlanDialog.value?.let { plan ->
        AlertDialog(
            onDismissRequest = { showDeletePlanDialog.value = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("Egzersiz Planını Sil") },
            text = {
                Text("\"${plan.title}\" planını silmek istediğinize emin misiniz? Bu işlem geri alınamaz.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(ExerciseManagementEvent.DeleteExercisePlan(plan.id))
                        showDeletePlanDialog.value = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeletePlanDialog.value = null }
                ) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Egzersiz Yönetimi",
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
                ),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
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
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
            // Kategori ve Zorluk Filtreleri - Sadece "Egzersizlerim" tabında göster
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
            // Tab Row
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                TabRow(
                    selectedTabIndex = selectedTab.value,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.value]),
                            height = 3.dp,
                            color = MaterialTheme.colorScheme.primary
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
                                    imageVector = if (index == 0) Icons.Default.FitnessCenter else Icons.Default.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            // Loading indicator
            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Error message
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
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            if (selectedTab.value == 0) {
                                viewModel.onEvent(ExerciseManagementEvent.LoadExercises)
                            } else {
                                viewModel.onEvent(ExerciseManagementEvent.LoadExercisePlans)
                            }
                        }) {
                            Text("Yeniden Dene")
                        }
                    }
                }
            }
            // Content
            if (!state.isLoading && state.errorMessage == null) {
                when (selectedTab.value) {
                    0 -> ExercisesTab(
                        exercises = if (state.selectedCategory.isEmpty() && state.selectedDifficulty == null)
                            state.exercises
                        else
                            state.filteredExercises,
                        onEditExercise = { exercise ->
                            // Düzenleme sayfasına git
                            try {
                                navController.navigate("edit_exercise_screen/${exercise.id}")
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Navigasyon hatası: ${e.message}")
                                }
                            }
                        },
                        onDeleteExercise = { exercise ->
                            // Silme işlemi
                            viewModel.onEvent(ExerciseManagementEvent.DeleteExercise(exercise.id))
                        }
                    )
                    1 -> ExercisePlansTab(
                        plans = state.exercisePlans,
                        patientNames = state.patientNames,
                        onPlanClick = { plan ->
                            // Navigate to edit plan screen
                            navController.navigate("edit_exercise_plan_screen/${plan.id}")
                        },
                        onDeletePlan = { plan ->
                            showDeletePlanDialog.value = plan
                        }
                    )
                }
            }
            // Success message
            if (state.actionSuccess != null) {
                LaunchedEffect(state.actionSuccess) {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = state.actionSuccess,
                            actionLabel = "Tamam",
                            duration = SnackbarDuration.Short
                        )
                        // Snackbar kapatıldıktan sonra
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
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // Kategori başlığı
            Text(
                text = "Kategoriler",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Kategori filtreleri
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory.isEmpty(),
                        onClick = { onCategoryClick("") },
                        label = { Text("Tümü") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtre",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { onCategoryClick(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Zorluk başlığı
            Text(
                text = "Zorluk Seviyesi",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // Zorluk seviyesi filtreleri
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tümü
                FilterChip(
                    selected = selectedDifficulty == null,
                    onClick = { onDifficultySelected(null) },
                    label = { Text("Tümü") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                // Kolay
                FilterChip(
                    selected = selectedDifficulty == ExerciseDifficulty.EASY,
                    onClick = { onDifficultySelected(ExerciseDifficulty.EASY) },
                    label = { Text("Kolay") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                // Orta
                FilterChip(
                    selected = selectedDifficulty == ExerciseDifficulty.MEDIUM,
                    onClick = { onDifficultySelected(ExerciseDifficulty.MEDIUM) },
                    label = { Text("Orta") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
                // Zor
                FilterChip(
                    selected = selectedDifficulty == ExerciseDifficulty.HARD,
                    onClick = { onDifficultySelected(ExerciseDifficulty.HARD) },
                    label = { Text("Zor") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
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
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick(exercise) }
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with gradient overlay
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
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
                    // Add a subtle gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            // Content with improved typography
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exercise.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Category chip
                Surface(
                    modifier = Modifier.padding(vertical = 4.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text = exercise.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Exercise details in a more compact horizontal flow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ) {
                    // Zorluk seviyesi bilgisi ekleyelim
                    val difficultyText = when (exercise.difficulty) {
                        ExerciseDifficulty.EASY -> "Kolay"
                        ExerciseDifficulty.MEDIUM -> "Orta"
                        ExerciseDifficulty.HARD -> "Zor"
                    }
                    val difficultyColor = when (exercise.difficulty) {
                        ExerciseDifficulty.EASY -> MaterialTheme.colorScheme.primary
                        ExerciseDifficulty.MEDIUM -> MaterialTheme.colorScheme.tertiary
                        ExerciseDifficulty.HARD -> MaterialTheme.colorScheme.error
                    }
                    ExerciseDetailChip(
                        icon = Icons.Default.Star,
                        text = difficultyText,
                        iconTint = difficultyColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            // Delete button with animation
            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    // Modern delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = {
                Text(
                    "Egzersizi Sil",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "\"${exercise.title}\" egzersizini silmek istediğinize emin misiniz? Bu işlem geri alınamaz.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeleteClick(exercise)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun ExerciseDetailChip(
    icon: ImageVector,
    text: String,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExercisePlansTab(
    plans: List<ExercisePlan>,
    patientNames: Map<String, String>, // Map of patient IDs to names
    onPlanClick: (ExercisePlan) -> Unit,
    onDeletePlan: (ExercisePlan) -> Unit
) {
    if (plans.isEmpty()) {
        EmptyState(
            message = "Henüz hiç egzersiz planı oluşturmadınız.",
            icon = Icons.Default.Assignment
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

    // Determine if plan is expired (end date has passed)
    val isExpired = plan.endDate?.before(currentDate) ?: false

    // Status text and color based on plan status and expiration
    val (statusColor, statusText) = when {
        isExpired -> Pair(MaterialTheme.colorScheme.error, "Pasif")
        plan.status == ExercisePlanStatus.ACTIVE -> Pair(MaterialTheme.colorScheme.primary, "Aktif")
        plan.status == ExercisePlanStatus.COMPLETED -> Pair(MaterialTheme.colorScheme.tertiary, "Tamamlandı")
        plan.status == ExercisePlanStatus.CANCELLED -> Pair(MaterialTheme.colorScheme.error, "İptal Edildi")
        else -> Pair(MaterialTheme.colorScheme.outline, plan.status.name)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onPlanClick(plan) },
        shape = RoundedCornerShape(16.dp),
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
            // Title row with delete button
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
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status badge
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

                // Delete button
                IconButton(
                    onClick = { onDeleteClick(plan) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            if (plan.description.isNotBlank()) {
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Patient info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hasta: $patientName",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${plan.exercises.size} egzersiz",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date and frequency info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
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
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (plan.frequency.isNotBlank()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = plan.frequency,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(120.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Egzersizlerinizi buradan yönetebilirsiniz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}