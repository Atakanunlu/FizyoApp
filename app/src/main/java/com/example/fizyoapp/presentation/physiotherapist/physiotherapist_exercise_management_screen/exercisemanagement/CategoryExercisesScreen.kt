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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val accentColor = Color(59, 62, 104)
private val textColor = Color.DarkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExercisesScreen(
    navController: NavController,
    viewModel: CategoryExercisesViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.category?.name ?: "Kategori Egzersizleri",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
            FloatingActionButton(
                onClick = { navController.navigate(AppScreens.CreateExercisePlanScreen.route) },
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
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
                    CircularProgressIndicator(
                        color = primaryColor,
                        strokeWidth = 3.dp
                    )
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
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = primaryColor
                        )
                    }

                    items(state.exercises) { exercise ->
                        ExerciseItem(exercise = exercise)
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            if (state.error != null) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissError() },
                    title = {
                        Text(
                            "Hata",
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = { Text(state.error) },
                    confirmButton = {
                        Button(
                            onClick = { viewModel.dismissError() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Tamam")
                        }
                    },
                    containerColor = surfaceColor
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
                tint = primaryColor.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bu kategoride henüz egzersiz yok",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = textColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sağ alttaki + butonuna tıklayarak yeni bir egzersiz ekleyebilirsiniz",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ExerciseItem(exercise: Exercise) {
    var expanded by remember { mutableStateOf(false) }
    var showMediaViewer by remember { mutableStateOf(false) }
    var selectedMediaUrl by remember { mutableStateOf("") }
    var selectedMediaType by remember { mutableStateOf("") }

    val exerciseType = remember(exercise) {
        if (exercise.mediaUrls.isEmpty()) {
            null
        } else {
            val firstMediaType = exercise.mediaType.values.firstOrNull()
            when (firstMediaType) {
                ExerciseType.VIDEO -> ExerciseType.VIDEO
                else -> ExerciseType.IMAGE
            }
        }
    }

    val firstMediaUrl = remember(exercise) { exercise.mediaUrls.firstOrNull() ?: "" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryColor.copy(alpha = 0.1f))
                ) {
                    if (firstMediaUrl.isNotEmpty()) {
                        AsyncImage(
                            model = firstMediaUrl,
                            contentDescription = exercise.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (exerciseType == ExerciseType.VIDEO) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = exercise.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = exercise.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = if (expanded) 10 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Daralt" else "Genişlet",
                        tint = primaryColor
                    )
                }
            }

            if (expanded && firstMediaUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = textColor.copy(alpha = 0.1f))
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
                                    .background(surfaceColor.copy(alpha = 0.7f))
                                    .clickable {
                                        showMediaViewer = true
                                        selectedMediaUrl = firstMediaUrl
                                        selectedMediaType = "video"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Oynat",
                                    tint = primaryColor,
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
                    else -> {}
                }
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