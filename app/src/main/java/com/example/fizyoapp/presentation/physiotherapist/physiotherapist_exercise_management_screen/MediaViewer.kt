package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_exercise_management_screen


import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@Composable
fun MediaViewer(
    mediaUrl: String,
    mediaType: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
            when {
                mediaType.contains("video") -> {
                    // Video görüntüleyici
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                // Harici video oynatıcı ile aç
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(Uri.parse(mediaUrl), "video/*")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Video oynatıcı bulunamadı
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Video önizleme
                        AsyncImage(
                            model = mediaUrl,
                            contentDescription = "Video",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Play butonu
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(40.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Oynat",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                else -> {
                    // Resim görüntüleyici
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = "Görsel",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Kapat butonu
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}