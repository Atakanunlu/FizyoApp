package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fizyoapp.domain.model.exercisemanagescreen.ExerciseType

private val primaryColor = Color(59, 62, 104)

@Composable
fun MediaViewer(
    mediaUrl: String,
    mediaType: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isVideo = mediaType == "video"
    var isPlaying by remember { mutableStateOf(false) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
        ) {
            if (isVideo) {
                if (isPlaying) {
                    // Dahili video oynatıcı
                    VideoPlayerMedia3(
                        videoUri = mediaUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Video önizleme - oynatma düğmesi
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { isPlaying = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.8f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Oynat",
                                tint = primaryColor,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Text(
                            text = "Videoyu oynatmak için tıklayın",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    } }
            } else {
                // Resim gösterimi
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = "Görsel",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Kapat butonu - video oynatılmıyorsa göster
            if (!isPlaying || !isVideo) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(48.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = Color.Black
                )
                }
            }
        }
    }
}
@Composable
fun VideoPlayerMedia3(
    videoUri: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
        }
    }
    DisposableEffect(key1 = Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
    Box(modifier = modifier) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}