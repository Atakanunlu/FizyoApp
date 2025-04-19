package com.example.fizyoapp.presentation.user.ornekegzersizler.buttons

import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.viewmodel.HipExercisesOfExamplesViewModel


import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.viewmodel.LegExercisesOfExamplesViewModel

import android.net.Uri
import android.util.Log
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
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.user.ornekegzersizler.buttons.data.viewmodel.ShoulderExercisesOfExamplesViewModel
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView


@Composable
fun HipExercisesScreen(navController: NavController) {
    val viewModel: HipExercisesOfExamplesViewModel = hiltViewModel()
    val videoList = viewModel.videoList.collectAsState()
    var currentIndex by remember { mutableStateOf(0) }

    // Başlangıçta videoları yükle
    LaunchedEffect(Unit) {
        viewModel.loadVideos()
    }

    // Video listesi değiştiğinde index kontrolü
    LaunchedEffect(videoList.value) {
        if (videoList.value.isNotEmpty() && currentIndex >= videoList.value.size) {
            currentIndex = 0
        }
    }

    // Şu anki video URI'sini key olarak kullan
    val currentVideoUri = remember(currentIndex, videoList.value) {
        if (videoList.value.isNotEmpty()) {
            Uri.parse(videoList.value[currentIndex].uri)
        } else {
            null
        }
    }

    // Şu anki video açıklamasını hatırla
    val currentDescription = remember(currentIndex, videoList.value) {
        if (videoList.value.isNotEmpty()) {
            videoList.value[currentIndex].description
        } else {
            ""
        }
    }

    // Videonun değiştiğini loglayarak doğrula
    LaunchedEffect(currentIndex) {
        if (videoList.value.isNotEmpty()) {
            Log.d("ShoulderExercises", "Changed to video: ${currentIndex + 1}, URI: ${videoList.value[currentIndex].uri}")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (videoList.value.isNotEmpty() && currentVideoUri != null) {
            // Video sayısı ve mevcut video indeksi gösterimi
            Text(
                text = "Video ${currentIndex + 1} / ${videoList.value.size}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(10.dp)
            ) {
                // Her URI değişiminde yeni bir ExoPlayerView oluştur
                key(currentVideoUri.toString()) {
                    HipExoPlayerView(videoUri = currentVideoUri)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = currentDescription,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Video navigasyon kontrolleri
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Önceki video butonu
                if (videoList.value.size > 1) {
                    IconButton(
                        onClick = {
                            // Önceki indexe git
                            currentIndex = if (currentIndex > 0)
                                currentIndex - 1
                            else
                                videoList.value.size - 1
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous Video"
                        )
                    }
                }

                // Sonraki video butonu
                IconButton(
                    onClick = {
                        // Sonraki indexe git
                        currentIndex = (currentIndex + 1) % videoList.value.size
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Video"
                    )
                }
            }
        } else {
            // Videolar yüklenirken göster
            CircularProgressIndicator()
            Text(
                "Videolar yükleniyor...",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
@Composable
fun HipExoPlayerView(videoUri: Uri) {
    val context = LocalContext.current

    // Video URI'sini log'a yazdır
    LaunchedEffect(videoUri) {
        Log.d("ExoPlayerView", "Loading new video: $videoUri")
    }

    // Exoplayer'ı URI değişince tamamen yeniden oluştur
    val exoPlayer = remember(videoUri.toString()) {  // toString() ile string olarak karşılaştır
        Log.d("ExoPlayerView", "Creating new ExoPlayer for URI: $videoUri")
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            repeatMode = com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
            prepare()
            playWhenReady = true  // Burada otomatik oynatmayı aktif et
        }
    }

    // Composable'dan çıkıldığında kaynakları temizle
    DisposableEffect(Unit) {
        onDispose {
            Log.d("ExoPlayerView", "Releasing ExoPlayer")
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            StyledPlayerView(ctx).apply {
                player = exoPlayer
                useController = true
                controllerShowTimeoutMs = 3000

                // ExoPlayer'ın durumunu dinle
                exoPlayer.addListener(object : com.google.android.exoplayer2.Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            com.google.android.exoplayer2.Player.STATE_READY -> {
                                Log.d("ExoPlayerView", "Player ready to play")
                            }
                            com.google.android.exoplayer2.Player.STATE_ENDED -> {
                                Log.d("ExoPlayerView", "Playback ended")
                                // Video bitince otomatik tekrar başlat (isteğe bağlı)
                                exoPlayer.seekTo(0)
                                exoPlayer.play()
                            }
                            com.google.android.exoplayer2.Player.STATE_BUFFERING -> {
                                Log.d("ExoPlayerView", "Buffering video")
                            }
                            com.google.android.exoplayer2.Player.STATE_IDLE -> {
                                Log.d("ExoPlayerView", "Player idle")
                            }
                        }
                    }
                })
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        // Anahtar değiştiğinde AndroidView'ı yeniden oluştur
        update = { playerView ->
            // Player yeniden oluşturulduğunda PlayerView'a atanır
            playerView.player = exoPlayer
        }
    )
}