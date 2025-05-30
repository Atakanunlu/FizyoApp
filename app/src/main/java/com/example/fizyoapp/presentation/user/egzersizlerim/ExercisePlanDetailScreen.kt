package com.example.fizyoapp.presentation.user.egzersizlerim
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlan
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExercisePlanItem
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseType
import com.example.fizyoapp.presentation.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
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
    var currentExerciseIndex by remember { mutableStateOf(0) }
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var currentMediaType by remember { mutableStateOf("image") }
    var currentMediaUrl by remember { mutableStateOf("") }
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
    BackHandler { navigateBack() }
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            playerView?.player = null
            exoPlayer.release()
        }
    }
    LaunchedEffect(planId) {
        viewModel.loadExercisePlan(planId)
    }
    // Herhangi bir kaynaktan medya seçimini işlemek için fonksiyon
    val handleMediaSelection = { mediaUrl: String, type: String ->
        currentMediaUrl = mediaUrl
        currentMediaType = type
        if (type == "video") {
            showPlayer = true
            val videoUri = Uri.parse(mediaUrl)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = isPlaying
        } else {
            showPlayer = false
        }
    }
    LaunchedEffect(currentExerciseIndex, state.plan) {
        if (state.plan != null && state.plan.exercises.isNotEmpty()) {
            val currentExercise = state.plan.exercises[currentExerciseIndex]
            if (currentExercise.mediaUrls.isNotEmpty()) {
                val mediaUrl = currentExercise.mediaUrls.first()
                currentMediaUrl = mediaUrl

                val isVideoFromType = currentExercise.mediaTypes[mediaUrl]?.name?.equals("VIDEO", ignoreCase = true) ?: false
                val isVideoFromUrl = mediaUrl.contains("video", ignoreCase = true) ||
                        mediaUrl.endsWith(".mp4", ignoreCase = true) ||
                        mediaUrl.endsWith(".mov", ignoreCase = true) ||
                        mediaUrl.endsWith(".webm", ignoreCase = true) ||
                        mediaUrl.endsWith(".avi", ignoreCase = true) ||
                        mediaUrl.endsWith(".3gp", ignoreCase = true) ||
                        mediaUrl.endsWith(".mkv", ignoreCase = true)
                val isVideo = isVideoFromType || isVideoFromUrl
                val mediaType = if (isVideo) "video" else "image"
                handleMediaSelection(mediaUrl, mediaType)
            } else {
                showPlayer = false
                currentMediaType = "none"
                currentMediaUrl = ""
            }
        }
    }
    DisposableEffect(Unit) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                val stateStr = when (state) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                Log.d("ExoPlayer", "State: $stateStr")
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Log.e("ExoPlayer", "Error: ${error.message}")
            }
        })
        onDispose {
            exoPlayer.stop()
            playerView?.player = null
            exoPlayer.release()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.plan?.title ?: "Egzersiz Planı",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri Dön",
                            tint = Color.White
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
                LoadingViewRedesigned()
            } else if (state.errorMessage != null) {
                ErrorViewRedesigned(state.errorMessage) {
                    viewModel.loadExercisePlan(planId)
                }
            } else if (state.plan == null) {
                ErrorViewRedesigned("Plan bulunamadı") {
                    viewModel.loadExercisePlan(planId)
                }
            } else if (state.plan.exercises.isEmpty()) {
                EmptyExercisesViewRedesigned()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Egzersiz Detayları",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Egzersiz programınızı düzenli takip edin",
                        fontSize = 16.sp,
                        color = textColor.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    PlanInfoCardRedesigned(state.plan)
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(primaryColor.copy(alpha = 0.1f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(
                                    fraction = (currentExerciseIndex + 1).toFloat() / state.plan.exercises.size
                                )
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(primaryColor, accentColor)
                                    )
                                )
                                .align(Alignment.CenterStart)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Egzersiz ${currentExerciseIndex + 1} / ${state.plan.exercises.size}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    MediaContainerRedesigned(
                        currentExercise = state.plan.exercises[currentExerciseIndex],
                        mediaType = currentMediaType,
                        showPlayer = showPlayer,
                        exoPlayer = exoPlayer,
                        onPlayerViewCreated = { playerView = it },
                        onMediaSelected = { url, type -> handleMediaSelection(url, type) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    ExerciseDetailsCardRedesigned(
                        exercise = state.plan.exercises[currentExerciseIndex]
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    ExerciseControlButtonsRedesigned(
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
                        showPlayPause = currentMediaType == "video" && showPlayer,
                        mediaType = currentMediaType
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = backgroundColor.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 0.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Her egzersizi uygun duruş ve nefes tekniğiyle yaparak en iyi sonuçları alabilirsiniz.",
                                fontSize = 14.sp,
                                color = textColor,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun LoadingViewRedesigned() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = primaryColor,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(60.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Egzersiz planı yükleniyor...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lütfen bekleyin",
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorViewRedesigned(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(errorColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = errorColor,
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Bir Hata Oluştu",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            fontSize = 16.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tekrar Dene",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyExercisesViewRedesigned() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(primaryColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.FitnessCenter,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Egzersiz Bulunamadı",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Bu planda henüz egzersiz bulunmuyor. Fizyoterapistinizle iletişime geçebilirsiniz.",
            fontSize = 16.sp,
            color = textColor.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Fizyoterapistinizden egzersiz planınıza egzersiz eklemesini isteyebilirsiniz.",
                    fontSize = 14.sp,
                    color = textColor,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

@Composable
fun PlanInfoCardRedesigned(plan: ExercisePlan) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SportsGymnastics,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = plan.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    if (plan.description.isNotBlank()) {
                        Text(
                            text = plan.description,
                            fontSize = 14.sp,
                            color = textColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = backgroundColor)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PlanInfoItem(
                    icon = Icons.Default.DateRange,
                    title = "Tarih Aralığı",
                    value = if (plan.startDate != null && plan.endDate != null) {
                        "${dateFormat.format(plan.startDate)} - ${dateFormat.format(plan.endDate)}"
                    } else {
                        "Belirtilmedi"
                    }
                )
                PlanInfoItem(
                    icon = Icons.Default.Repeat,
                    title = "Sıklık",
                    value = plan.frequency
                )
            }
            if (!plan.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = backgroundColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Fizyoterapist Notları",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = plan.notes,
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.8f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun PlanInfoItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = primaryColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 22.dp)
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun MediaContainerRedesigned(
    currentExercise: ExercisePlanItem,
    mediaType: String,
    showPlayer: Boolean,
    exoPlayer: ExoPlayer,
    onPlayerViewCreated: (PlayerView) -> Unit,
    onMediaSelected: (String, String) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
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
            Text(
                text = "Egzersiz Gösterimi",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = backgroundColor.copy(alpha = 0.7f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (mediaType) {
                    "video" -> {
                        if (showPlayer) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        player = exoPlayer
                                        useController = true
                                        controllerShowTimeoutMs = 3000
                                        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                                        useController = true
                                        onPlayerViewCreated(this)
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                update = { view ->
                                    view.player = exoPlayer
                                }
                            )
                        } else {
                            CircularProgressIndicator(color = primaryColor)
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
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FitnessCenter,
                                contentDescription = null,
                                tint = primaryColor.copy(alpha = 0.3f),
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Görsel içerik bulunamadı",
                                color = textColor.copy(alpha = 0.5f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (mediaType) {
                    "video" -> {
                        Icon(
                            imageVector = Icons.Rounded.Videocam,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Video gösterimi",
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                    "image" -> {
                        Icon(
                            imageVector = Icons.Rounded.Image,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Görsel gösterimi",
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Rounded.ImageNotSupported,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Görsel içerik yok",
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                }
            }
            if (currentExercise.mediaUrls.size > 1) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = backgroundColor)
                Spacer(modifier = Modifier.height(8.dp))
                MediaGallery(
                    mediaUrls = currentExercise.mediaUrls,
                    mediaTypes = currentExercise.mediaTypes,
                    onMediaSelected = onMediaSelected
                )
            }
        }
    }
}

@Composable
fun MediaGallery(
    mediaUrls: List<String>,
    mediaTypes: Map<String, ExerciseType>,
    onMediaSelected: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (mediaUrls.isNotEmpty()) {
            Text(
                text = "Tüm Görseller (${mediaUrls.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = primaryColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
            ) {
                items(mediaUrls) { mediaUrl ->
                    val isVideo = mediaTypes[mediaUrl] == ExerciseType.VIDEO ||
                            mediaUrl.contains("video") ||
                            mediaUrl.endsWith(".mp4") ||
                            mediaUrl.endsWith(".mov") ||
                            mediaUrl.endsWith(".webm") ||
                            mediaUrl.endsWith(".avi")
                    val mediaType = if (isVideo) "video" else "image"
                    MediaGalleryItem(
                        mediaUrl = mediaUrl,
                        mediaType = mediaType,
                        onClick = { onMediaSelected(mediaUrl, mediaType) }
                    )
                }
            }
        }
    }
}

@Composable
fun MediaGalleryItem(
    mediaUrl: String,
    mediaType: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
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
                .data(mediaUrl)
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
                            overlayColor.copy(alpha = 0.1f),
                            overlayColor.copy(alpha = 0.3f)
                        ),
                        startY = 0f,
                        endY = 80f
                    )
                )
        )
        if (mediaType == "video") {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(30.dp)
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
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ExerciseDetailsCardRedesigned(
    exercise: ExercisePlanItem
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = exercise.exerciseTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ExerciseParameterRedesigned(
                    label = "Set",
                    value = exercise.sets.toString(),
                    icon = Icons.Rounded.Repeat,
                    color = primaryColor
                )
                ExerciseParameterRedesigned(
                    label = "Tekrar",
                    value = exercise.repetitions.toString(),
                    icon = Icons.Rounded.Numbers,
                    color = greenColor
                )
                ExerciseParameterRedesigned(
                    label = "Süre (sn)",
                    value = exercise.duration.toString(),
                    icon = Icons.Rounded.Timer,
                    color = warningColor
                )
            }
            if (exercise.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = backgroundColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Egzersiz Açıklaması",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = exercise.notes,
                    fontSize = 15.sp,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun ExerciseParameterRedesigned(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ExerciseControlButtonsRedesigned(
    currentIndex: Int,
    totalExercises: Int,
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    showPlayPause: Boolean,
    mediaType: String = ""
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Egzersiz Kontrolü",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = primaryColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = showPlayPause && mediaType == "video",
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = onPrevious,
                    containerColor = backgroundColor.copy(alpha = 0.7f),
                    contentColor = primaryColor,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Önceki Egzersiz",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            AnimatedVisibility(
                visible = showPlayPause,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = onPlayPause,
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
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
            FloatingActionButton(
                onClick = onNext,
                containerColor = primaryColor,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = "Sonraki Egzersiz",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        if (totalExercises > 1) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sonraki egzersiz için sağa kaydırın",
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}