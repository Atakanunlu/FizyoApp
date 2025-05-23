
package com.example.fizyoapp.presentation.user.egzersizlerim

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fizyoapp.domain.model.exercise.ExercisePlanItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun ExercisePlanDetailScreen(
    navController: NavController,
    planId: String,
    viewModel: ExercisePlanDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Material3 renklerine daha uygun bir renk paleti
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background
    val accentColor = MaterialTheme.colorScheme.secondary
    val secondaryAccent = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surfaceVariant

    var currentExerciseIndex by remember { mutableStateOf(0) }

    // ExoPlayer kurulumu
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }

    // Medya tipi (video veya resim)
    var currentMediaType by remember { mutableStateOf("image") }

    // Geri navigasyon fonksiyonu
    val navigateBack = {
        coroutineScope.launch {
            showPlayer = false
            exoPlayer.stop()
            playerView?.player = null
            playerView = null
            exoPlayer.release()
            navController.navigateUp()
        }
    }

    // Back tuşu kontrolü
    BackHandler { navigateBack() }

    // Player için temizleme
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            playerView?.player = null
            exoPlayer.release()
        }
    }

    // Planı yükle
    LaunchedEffect(planId) {
        viewModel.loadExercisePlan(planId)
    }

    // Egzersiz değiştiğinde media içeriğini ayarla
    LaunchedEffect(currentExerciseIndex, state.plan) {
        if (state.plan != null && state.plan.exercises.isNotEmpty()) {
            val currentExercise = state.plan.exercises[currentExerciseIndex]
            if (currentExercise.mediaUrls.isNotEmpty()) {
                val mediaUrl = currentExercise.mediaUrls.first()

                // Video mu resim mi olduğunu kontrol et
                currentMediaType = if (mediaUrl.contains("video") ||
                    mediaUrl.contains(".mp4") ||
                    mediaUrl.contains(".mov")) {
                    // Video ise ExoPlayer'ı hazırla
                    showPlayer = true
                    val videoUri = Uri.parse(mediaUrl)
                    exoPlayer.stop()
                    exoPlayer.clearMediaItems()
                    exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
                    exoPlayer.prepare()
                    exoPlayer.playWhenReady = isPlaying
                    "video"
                } else {
                    // Resim ise player'ı gizle
                    showPlayer = false
                    "image"
                }
            } else {
                showPlayer = false
                currentMediaType = "none"
            }
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.plan?.title ?: "Egzersiz Planı",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri Dön",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
        ) {
            if (state.isLoading) {
                LoadingView(accentColor)
            } else if (state.errorMessage != null) {
                ErrorView(state.errorMessage, accentColor) {
                    viewModel.loadExercisePlan(planId)
                }
            } else if (state.plan == null) {
                ErrorView("Plan bulunamadı", accentColor) {
                    viewModel.loadExercisePlan(planId)
                }
            } else if (state.plan.exercises.isEmpty()) {
                EmptyExercisesView()
            } else {
                // Plan detayları ve egzersizler
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Plan bilgileri
                    PlanInfoCard(state.plan)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mevcut egzersiz göstergesi
                    Text(
                        text = "Egzersiz ${currentExerciseIndex + 1} / ${state.plan.exercises.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // İlerleme göstergesi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (i in state.plan.exercises.indices) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(
                                        width = if (i == currentExerciseIndex) 20.dp else 8.dp,
                                        height = 8.dp
                                    )
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (i == currentExerciseIndex) accentColor
                                        else accentColor.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Medya bölümü (video veya resim)
                    MediaContainer(
                        currentExercise = state.plan.exercises[currentExerciseIndex],
                        mediaType = currentMediaType,
                        showPlayer = showPlayer,
                        exoPlayer = exoPlayer,
                        onPlayerViewCreated = { playerView = it },
                        accentColor = accentColor,
                        secondaryAccent = secondaryAccent,
                        cardColor = cardColor
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Egzersiz detayları
                    ExerciseDetailsCard(
                        exercise = state.plan.exercises[currentExerciseIndex],
                        cardColor = cardColor,
                        textColor = textColor
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Kontrol butonları
                    ExerciseControlButtons(
                        currentIndex = currentExerciseIndex,
                        totalExercises = state.plan.exercises.size,
                        isPlaying = isPlaying,
                        onPrevious = {
                            currentExerciseIndex = if (currentExerciseIndex > 0)
                                currentExerciseIndex - 1
                            else
                                state.plan.exercises.size - 1
                        },
                        onPlayPause = {
                            isPlaying = !isPlaying
                            exoPlayer.playWhenReady = isPlaying
                        },
                        onNext = {
                            currentExerciseIndex = (currentExerciseIndex + 1) % state.plan.exercises.size
                        },
                        accentColor = accentColor,
                        cardColor = cardColor,
                        textColor = textColor,
                        showPlayPause = currentMediaType == "video" && showPlayer
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun LoadingView(accentColor: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = accentColor,
                modifier = Modifier.size(60.dp),
                strokeWidth = 5.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Egzersiz planı yükleniyor...",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ErrorView(message: String, accentColor: Color, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                )
            ) {
                Text("Tekrar Dene")
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
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bu planda henüz egzersiz bulunmuyor",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PlanInfoCard(plan: com.example.fizyoapp.domain.model.exercise.ExercisePlan) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Plan başlığı
            Text(
                text = plan.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Plan açıklaması
            if (plan.description.isNotBlank()) {
                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tarih bilgisi
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val dateText = if (plan.startDate != null && plan.endDate != null) {
                    "${dateFormat.format(plan.startDate)} - ${dateFormat.format(plan.endDate)}"
                } else {
                    "Tarih belirtilmedi"
                }
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Sıklık bilgisi
            if (plan.frequency.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = plan.frequency,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Notlar
            if (!plan.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notlar:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = plan.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MediaContainer(
    currentExercise: ExercisePlanItem,
    mediaType: String,
    showPlayer: Boolean,
    exoPlayer: ExoPlayer,
    onPlayerViewCreated: (PlayerView) -> Unit,
    accentColor: Color,
    secondaryAccent: Color,
    cardColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(accentColor, secondaryAccent)
                )
            )
            .padding(3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(cardColor)
        ) {
            when (mediaType) {
                "video" -> {
                    if (showPlayer) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = true
                                    controllerShowTimeoutMs = 1500
                                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                                    onPlayerViewCreated(this)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                "image" -> {
                    if (currentExercise.mediaUrls.isNotEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(currentExercise.mediaUrls.first())
                                .crossfade(true)
                                .build(),
                            contentDescription = currentExercise.exerciseTitle,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                else -> {
                    // Medya yoksa placeholder göster
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseDetailsCard(
    exercise: ExercisePlanItem,
    cardColor: Color,
    textColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Egzersiz başlığı
            Text(
                text = exercise.exerciseTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Egzersiz parametreleri
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ExerciseParameter(
                    label = "Set",
                    value = exercise.sets.toString(),
                    icon = Icons.Rounded.Repeat
                )

                ExerciseParameter(
                    label = "Tekrar",
                    value = exercise.repetitions.toString(),
                    icon = Icons.Rounded.Numbers
                )

                ExerciseParameter(
                    label = "Süre (sn)",
                    value = exercise.duration.toString(),
                    icon = Icons.Rounded.Timer
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Egzersiz notları
            if (exercise.notes.isNotBlank()) {
                Text(
                    text = exercise.notes,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun ExerciseParameter(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ExerciseControlButtons(
    currentIndex: Int,
    totalExercises: Int,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    accentColor: Color,
    cardColor: Color,
    textColor: Color,
    showPlayPause: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Önceki egzersiz butonu
        AnimatedVisibility(
            visible = totalExercises > 1,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(
                onClick = onPrevious,
                containerColor = cardColor,
                contentColor = textColor,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Önceki Egzersiz",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Oynat/Duraklat butonu (sadece video varsa göster)
        AnimatedVisibility(
            visible = showPlayPause,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(
                onClick = onPlayPause,
                containerColor = accentColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(72.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                AnimatedContent(
                    targetState = isPlaying
                ) { playing ->
                    Icon(
                        imageVector = if (playing)
                            Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (playing) "Duraklat" else "Oynat",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // Sonraki egzersiz butonu
        FloatingActionButton(
            onClick = onNext,
            containerColor = cardColor,
            contentColor = textColor,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Sonraki Egzersiz",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}