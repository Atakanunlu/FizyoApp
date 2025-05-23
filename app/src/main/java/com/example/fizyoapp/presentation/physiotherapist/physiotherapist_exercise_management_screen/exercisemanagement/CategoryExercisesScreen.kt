package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.exercisemanagement

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.exercise.Exercise
import com.example.fizyoapp.domain.model.exercise.ExerciseType
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen.MediaViewer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExercisesScreen(
    navController: NavController,
    viewModel: CategoryExercisesViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.category?.name ?: "Kategori Egzersizleri",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(AppScreens.CreateExercisePlanScreen.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Egzersiz Planı Ekle")
            }
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
                    CircularProgressIndicator()
                }
            } else if (state.exercises.isEmpty()) {
                EmptyExercisesView()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${state.exercises.size} Egzersiz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(state.exercises) { exercise ->
                        ExerciseItem(exercise = exercise)
                    }

                    // FAB için boşluk
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // Error dialog
            if (state.error != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissError() },
                    title = { Text("Hata") },
                    text = { Text(state.error) },
                    confirmButton = {
                        Button(onClick = { viewModel.dismissError() }) {
                            Text("Tamam")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyExercisesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bu kategoride henüz egzersiz yok",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sağ alttaki + butonuna tıklayarak yeni bir egzersiz ekleyebilirsiniz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseItem(exercise: Exercise) {
    var expanded by remember { mutableStateOf(false) }
    var showMediaViewer by remember { mutableStateOf(false) }
    var selectedMediaUrl by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf("") }

    // Exercise.type alanını mediaType'dan belirleyelim
    val exerciseType = remember(exercise) {
        if (exercise.mediaUrls.isEmpty()) {
        } else {
            val firstMediaType = exercise.mediaType.values.firstOrNull()
            when (firstMediaType) {
                ExerciseType.VIDEO -> ExerciseType.VIDEO
                else -> ExerciseType.IMAGE
            }
        }
    }

    // İlk medya URL'ini al
    val firstMediaUrl = remember(exercise) { exercise.mediaUrls.firstOrNull() ?: "" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically  // verticalAliganment hatası düzeltildi
            ) {
                // Egzersiz medya önizlemesi
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    when (exerciseType) {
                        ExerciseType.VIDEO -> {
                            AsyncImage(
                                model = firstMediaUrl,
                                contentDescription = exercise.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black)
                                    .clickable {
                                        showMediaViewer = true
                                        selectedMediaUrl = firstMediaUrl
                                        selectedMediaType = "video"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        ExerciseType.IMAGE -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        showMediaViewer = true
                                        selectedMediaUrl = firstMediaUrl
                                        selectedMediaType = "image"
                                    }
                            ) {
                                AsyncImage(
                                    model = firstMediaUrl,
                                    contentDescription = exercise.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = exercise.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = if (expanded) 10 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Daralt" else "Genişlet"
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                when (exerciseType) {
                    ExerciseType.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .clickable {
                                    showMediaViewer = true
                                    selectedMediaUrl = firstMediaUrl
                                    selectedMediaType = "video"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Video oynatma butonu ve önizleme resmi
                            AsyncImage(
                                model = firstMediaUrl,
                                contentDescription = exercise.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                    .clickable { /* Video oynat */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Oynat",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                    ExerciseType.IMAGE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showMediaViewer = true
                                    selectedMediaUrl = firstMediaUrl
                                    selectedMediaType = "image"
                                }
                        ) {
                            AsyncImage(
                                model = firstMediaUrl,
                                contentDescription = exercise.title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

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