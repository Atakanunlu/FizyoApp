package com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.messagesscreen.Message
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// Radyolojik mesaj kontrol fonksiyonu
fun isRadiologicalImageMessage(message: Message): Boolean {
    return message.content.startsWith("[RADIOLOGICAL_IMAGE]")
}

// Tıbbi rapor mesaj kontrol fonksiyonu
fun isMedicalReportMessage(message: Message): Boolean {
    return message.content.startsWith("[MEDICAL_REPORT]")
}

// Radyolojik görüntü veri çıkarma
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

// Tıbbi rapor veri çıkarma
fun extractMedicalReportData(message: Message): MedicalReportData {
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

    val title = jsonContent?.optString("title") ?: "Tıbbi Rapor"
    val description = jsonContent?.optString("description") ?: ""
    val fileUrl = jsonContent?.optString("fileUrl") ?: ""
    val doctorName = jsonContent?.optString("doctorName") ?: ""
    val hospitalName = jsonContent?.optString("hospitalName") ?: ""
    val timestamp = try {
        val timeMs = jsonContent?.optLong("timestamp", 0) ?: 0
        if (timeMs > 0) Date(timeMs) else message.timestamp
    } catch (e: Exception) {
        message.timestamp
    }

    return MedicalReportData(
        title = title,
        description = description,
        fileUrl = fileUrl,
        doctorName = doctorName,
        hospitalName = hospitalName,
        timestamp = timestamp
    )
}

data class RadiologicalImageData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val timestamp: Date
)

data class MedicalReportData(
    val title: String,
    val description: String,
    val fileUrl: String,
    val doctorName: String = "",
    val hospitalName: String = "",
    val timestamp: Date
)

// Radyolojik görüntü mesaj balonu
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

// Tıbbi rapor mesaj balonu
@Composable
fun MedicalReportMessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    onClick: () -> Unit
) {
    val reportData = extractMedicalReportData(message)
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clickable {
                // Doğrudan PDF'i açmak için
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(reportData.fileUrl)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        "Rapor açılamadı: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Açılamazsa diyaloğu göster
                    onClick()
                }
            },
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
                text = reportData.title,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentUser) Color.White else Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = "PDF Rapor",
                        tint = if (isCurrentUser) Color(0xCCFFFFFF) else Color(59, 62, 104),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Tıbbi Rapor (PDF)",
                        color = if (isCurrentUser) Color.White else Color(59, 62, 104),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (reportData.doctorName.isNotEmpty() || reportData.hospitalName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))

                        val details = buildString {
                            if (reportData.doctorName.isNotEmpty()) {
                                append(reportData.doctorName)
                            }

                            if (reportData.doctorName.isNotEmpty() && reportData.hospitalName.isNotEmpty()) {
                                append(" • ")
                            }

                            if (reportData.hospitalName.isNotEmpty()) {
                                append(reportData.hospitalName)
                            }
                        }

                        Text(
                            text = details,
                            fontSize = 12.sp,
                            color = if (isCurrentUser) Color.White.copy(alpha = 0.8f)
                            else Color.Gray,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = Uri.parse(reportData.fileUrl)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Rapor açılamadı: ${e.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCurrentUser) Color.White.copy(alpha = 0.2f)
                            else Color(59, 62, 104)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Aç",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Görüntüle",
                            fontSize = 12.sp,
                            color = if (isCurrentUser) Color.White else Color.White
                        )
                    }
                }
            }

            if (reportData.description.isNotEmpty()) {
                Text(
                    text = reportData.description,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.9f)
                    else Color.Black.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            // Zaman
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(reportData.timestamp),
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

// Radyolojik görüntü detay diyaloğu
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


@Composable
fun MedicalReportDetailDialog(
    message: Message,
    onDismiss: () -> Unit
) {
    val reportData = extractMedicalReportData(message)
    val context = LocalContext.current

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
                        text = reportData.title,
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

                Spacer(modifier = Modifier.height(12.dp))

                if (reportData.doctorName.isNotEmpty() || reportData.hospitalName.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        if (reportData.doctorName.isNotEmpty()) {
                            Text(
                                text = "Doktor: ${reportData.doctorName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                        }

                        if (reportData.hospitalName.isNotEmpty()) {
                            Text(
                                text = "Hastane: ${reportData.hospitalName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(top = if (reportData.doctorName.isNotEmpty()) 4.dp else 0.dp)
                            )
                        }
                    }
                }

                if (reportData.description.isNotEmpty()) {
                    Text(
                        text = reportData.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = Color(59, 62, 104),
                                modifier = Modifier.size(64.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Tıbbi Rapor (PDF)",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(59, 62, 104)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse(reportData.fileUrl)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "Rapor açılamadı: ${e.localizedMessage}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(59, 62, 104)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Aç"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Raporu Aç")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tarih: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        .format(reportData.timestamp)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
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
}