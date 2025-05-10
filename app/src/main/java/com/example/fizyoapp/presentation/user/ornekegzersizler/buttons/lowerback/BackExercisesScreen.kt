package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.lowerback
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

@OptIn(ExperimentalMaterial3Api::class)
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun LowerBackExercisesScreen(navController: NavController) {
    val viewModel: LowerBackExercisesOfExamplesViewModel = hiltViewModel()
    val videoList = viewModel.videoList.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val primaryColor = Color(0xFF3B3E68)
    val backgroundColor = Color(0xFF2A2D47)
    val accentColor = Color(0xFF4EABCF)
    val secondaryAccent = Color(0xFF2A93B8)
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
                        "Bel Egzersizleri",
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
                            "Bel egzersizleri yükleniyor...",
                            color = textColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "Video ${currentIndex + 1} / ${videoList.value.size}",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            for (i in videoList.value.indices) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(
                                            width = if (i == currentIndex) 24.dp else 10.dp,
                                            height = 8.dp
                                        )
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (i == currentIndex) accentColor
                                            else accentColor.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }

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
                                            playerView = this
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cardColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Text(
                            text = currentDescription,
                            modifier = Modifier.padding(16.dp),
                            color = textColor,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(
                            visible = videoList.value.size > 1,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    currentIndex = if (currentIndex > 0)
                                        currentIndex - 1
                                    else
                                        videoList.value.size - 1
                                },
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
                            onClick = {
                                isPlaying = !isPlaying
                                exoPlayer.playWhenReady = isPlaying
                            },
                            containerColor = accentColor,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp),
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            AnimatedContent(targetState = isPlaying) { playing ->
                                Icon(
                                    imageVector = if (playing)
                                        Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (playing) "Duraklat" else "Oynat",
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        FloatingActionButton(
                            onClick = {
                                currentIndex = (currentIndex + 1) % videoList.value.size
                            },
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

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}