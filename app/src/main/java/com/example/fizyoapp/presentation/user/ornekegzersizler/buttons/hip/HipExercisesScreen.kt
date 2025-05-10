package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.hip
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HipExercisesScreen(navController: NavController) {
    val viewModel: HipExercisesOfExamplesViewModel = hiltViewModel()
    val videoList = viewModel.videoList.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val primaryColor = Color(0xFF3B3E68)
    val backgroundColor = Color(0xFF2A2D47)
    val accentColor = Color(0xFF6D72C3)
    val secondaryAccent = Color(0xFFE86CA6)
    val textColor = Color.White
    val cardColor = Color(0xFF343860)

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    var showPlayer by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }

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

    LaunchedEffect(Unit) {
        viewModel.loadVideos()
    }

    LaunchedEffect(currentIndex, videoList.value) {
        if (videoList.value.isNotEmpty()) {
            val videoUri = Uri.parse(videoList.value[currentIndex].uri)
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = isPlaying
        }
    }

    val currentDescription = remember(currentIndex, videoList.value) {
        videoList.value.getOrNull(currentIndex)?.description ?: ""
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kalça Egzersizleri",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri Dön",
                            tint = textColor
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
            if (videoList.value.isEmpty()) {
                LoadingView()
            } else {
                ContentView(
                    videoList = videoList.value,
                    currentIndex = currentIndex,
                    currentDescription = currentDescription,
                    showPlayer = showPlayer,
                    isPlaying = isPlaying,
                    exoPlayer = exoPlayer,
                    onPreviousClick = {
                        currentIndex = if (currentIndex > 0)
                            currentIndex - 1
                        else
                            videoList.value.size - 1
                    },
                    onPlayPauseClick = {
                        isPlaying = !isPlaying
                        exoPlayer.playWhenReady = isPlaying
                    },
                    onNextClick = {
                        currentIndex = (currentIndex + 1) % videoList.value.size
                    },
                    onPlayerViewCreated = { playerView = it },
                    accentColor = accentColor,
                    secondaryAccent = secondaryAccent,
                    cardColor = cardColor,
                    textColor = textColor
                )
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF6D72C3),
                modifier = Modifier.size(60.dp),
                strokeWidth = 5.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Videolar yükleniyor...",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ContentView(
    videoList: List<Any>,
    currentIndex: Int,
    currentDescription: String,
    showPlayer: Boolean,
    isPlaying: Boolean,
    exoPlayer: ExoPlayer,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayerViewCreated: (PlayerView) -> Unit,
    accentColor: Color,
    secondaryAccent: Color,
    cardColor: Color,
    textColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VideoProgressIndicator(
            currentIndex = currentIndex,
            totalVideos = videoList.size,
            accentColor = accentColor,
            textColor = textColor
        )
        Spacer(modifier = Modifier.height(16.dp))

        VideoPlayerCard(
            showPlayer = showPlayer,
            exoPlayer = exoPlayer,
            onPlayerViewCreated = onPlayerViewCreated,
            accentColor = accentColor,
            secondaryAccent = secondaryAccent,
            cardColor = cardColor
        )
        Spacer(modifier = Modifier.height(16.dp))

        DescriptionCard(
            description = currentDescription,
            cardColor = cardColor,
            textColor = textColor
        )
        Spacer(modifier = Modifier.weight(1f))

        ControlButtons(
            showPrevious = videoList.size > 1,
            isPlaying = isPlaying,
            onPreviousClick = onPreviousClick,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            accentColor = accentColor,
            cardColor = cardColor,
            textColor = textColor
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun VideoProgressIndicator(
    currentIndex: Int,
    totalVideos: Int,
    accentColor: Color,
    textColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = "Video ${currentIndex + 1} / $totalVideos",
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 0 until totalVideos) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = 24.dp, height = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (i == currentIndex) accentColor
                            else accentColor.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
private fun VideoPlayerCard(
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
    }
}

@Composable
private fun DescriptionCard(
    description: String,
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
        Text(
            text = description,
            modifier = Modifier.padding(16.dp),
            color = textColor,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun ControlButtons(
    showPrevious: Boolean,
    isPlaying: Boolean,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    accentColor: Color,
    cardColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = showPrevious,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(
                onClick = onPreviousClick,
                containerColor = cardColor,
                contentColor = textColor,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = "Önceki Video",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        FloatingActionButton(
            onClick = onPlayPauseClick,
            containerColor = accentColor,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(72.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp
            )
        ) {
            AnimatedContent(targetState = isPlaying) { isPlaying ->
                Icon(
                    imageVector = if (isPlaying)
                        Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        FloatingActionButton(
            onClick = onNextClick,
            containerColor = cardColor,
            contentColor = textColor,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.SkipNext,
                contentDescription = "Sonraki Video",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}