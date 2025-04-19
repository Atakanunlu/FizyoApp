package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.hip


import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer


@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HipExercisesScreen(navController: NavController) {
    val viewModel:HipExercisesOfExamplesViewModel= hiltViewModel()
    val videoList=viewModel.videoList.collectAsState()
    val context= LocalContext.current
    val coroutineScope= rememberCoroutineScope()
    var currentIndex by remember { mutableStateOf(0) }

    val exoPlayer= remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    var showPlayer by remember { mutableStateOf(true) }

    val navigateBack={
        coroutineScope.launch {
            showPlayer=false
            exoPlayer.stop()
            playerView?.player=null
            playerView=null
            exoPlayer.release()
            navController.navigateUp()

        }
    }

    BackHandler { navigateBack() }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            playerView?.player=null
            exoPlayer.release()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadVideos()
    }

    LaunchedEffect(currentIndex,videoList.value.size) {

        if(videoList.value.isNotEmpty()){
            val videoUri=Uri.parse(videoList.value[currentIndex].uri)

            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady=true
        }
    }

    val currentDescription=remember(currentIndex,videoList.value){
        videoList.value.getOrNull(currentIndex)?.description ?:""
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text("Kalça Egzersizleri")
            },
            navigationIcon = {
                IconButton(onClick = {navigateBack()}) {
                    Icon(Icons.Default.ArrowBack,"Geri Dön")
                }
            }
        )
    }) { paddingValues ->

        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (videoList.value.isNotEmpty()) {
                Text(
                    text = "Video ${currentIndex + 1} / ${videoList.value.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth() // Genişliği ekran genişliğine eşit
                        .height(300.dp) // Sabit yükseklik
                        .padding(10.dp) // İç boşluk
                ) {
                    if (showPlayer) {
                        AndroidView(
                            factory = { ctx ->
                                PlayerView(ctx).apply {
                                    player = exoPlayer
                                    useController = true
                                    useController = true
                                    controllerShowTimeoutMs = 1000
                                    playerView = this

                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                    }

                }
                Spacer(modifier = Modifier.height(10.dp))

                // Video açıklaması metni
                Text(
                    text = currentDescription, // Şu anki videonun açıklaması
                    fontSize = 18.sp, // Metin boyutu
                    textAlign = TextAlign.Center // Metni ortala
                )

                // Kontrol butonları için boşluk
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly // Butonları eşit aralıklarla yerleştir
                ) {
                    // Önceki video butonu - birden fazla video varsa gösterilir
                    if (videoList.value.size > 1) {
                        IconButton(
                            onClick = {
                                // Önceki videoya geç, ilk videodaysa sonuncuya dön (dairesel)
                                currentIndex = if (currentIndex > 0)
                                    currentIndex - 1
                                else
                                    videoList.value.size - 1
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous Video" // Ekran okuyucular için açıklama
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            // Sonraki videoya geç, son videodaysa ilk videoya dön (dairesel)
                            currentIndex = (currentIndex + 1) % videoList.value.size
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Next Video" // Ekran okuyucular için açıklama
                        )
                    }


                }

            }
            else {
                // Video listesi boşsa veya yüklenmediyse loading göstergesi
                CircularProgressIndicator() // Dönen yükleniyor animasyonu
                Text(
                    "Videolar yükleniyor...", // Yükleme durumu metni
                    modifier = Modifier.padding(16.dp)
                )
            }

        }
    }

}

