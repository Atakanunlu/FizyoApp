package com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.messagesscreen.Message
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


fun isRadiologicalImageMessage(message: Message): Boolean {
    return message.content.startsWith("[RADIOLOGICAL_IMAGE]")
}


fun extractRadiologicalImageData(message: Message): RadiologicalImageData {
    val jsonContent = try {
        val contentParts = message.content.split("\n", limit = 2)
        if (contentParts.size > 1) {
            JSONObject(contentParts[1])
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }

    val title = jsonContent?.optString("title") ?: "Radyolojik Görüntü"
    val description = jsonContent?.optString("description") ?: ""
    val imageUrl = jsonContent?.optString("url") ?: ""
    val timestamp = try {
        val timeMs = jsonContent?.optLong("timestamp", 0) ?: 0
        if (timeMs > 0) Date(timeMs) else message.timestamp
    } catch (e: Exception) {
        message.timestamp
    }

    return RadiologicalImageData(
        title = title,
        description = description,
        imageUrl = imageUrl,
        timestamp = timestamp
    )
}


data class RadiologicalImageData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val timestamp: Date
)


@Composable
fun RadiologicalImageMessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    onClick: () -> Unit
) {
    val imageData = extractRadiologicalImageData(message)

    Card(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(
            topStart = if (isCurrentUser) 12.dp else 0.dp,
            topEnd = if (isCurrentUser) 0.dp else 12.dp,
            bottomStart = 12.dp,
            bottomEnd = 12.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser)
                Color(59, 62, 104) else Color(0xFFEEEEEE)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {

            Text(
                text = imageData.title,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentUser) Color.White else Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0))
            ) {
                if (imageData.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = imageData.imageUrl,
                        contentDescription = imageData.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        error = ColorPainter(Color(0xFFE0E0E0)),
                        fallback = ColorPainter(Color(0xFFE0E0E0))
                    )
                } else {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }


            if (imageData.description.isNotEmpty()) {
                Text(
                    text = imageData.description,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.9f)
                    else Color.Black.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Zaman
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(imageData.timestamp),
                fontSize = 10.sp,
                color = if (isCurrentUser) Color.White.copy(alpha = 0.6f)
                else Color.Black.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp)
            )
        }
    }
}


@Composable
fun RadiologicalImageDetailDialog(
    message: Message,
    onDismiss: () -> Unit
) {
    val imageData = extractRadiologicalImageData(message)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = imageData.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF5F5F5))
                ) {
                    if (imageData.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = imageData.imageUrl,
                            contentDescription = imageData.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BrokenImage,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Görüntü yüklenemedi",
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))


                if (imageData.description.isNotEmpty()) {
                    Text(
                        text = imageData.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }


                Text(
                    text = "Tarih: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        .format(imageData.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))


                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(59, 62, 104)
                    )
                ) {
                    Text("Tamam")
                }
            }
        }
    }
}