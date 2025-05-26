package com.example.fizyoapp.presentation.bottomnavbar.items.messagesscreen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.messagesscreen.Message
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


fun isRadiologicalImageMessage(message: Message): Boolean {
    return message.content.startsWith("[RADIOLOGICAL_IMAGE]")
}


fun isMedicalReportMessage(message: Message): Boolean {
    return message.content.startsWith("[MEDICAL_REPORT]")
}

fun isEvaluationFormMessage(message: Message): Boolean {
    return message.content.startsWith("[EVALUATION_FORM]")
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
    val fileType = jsonContent?.optString("fileType") ?: "image"
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
        timestamp = timestamp,
        fileType = fileType
    )
}

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
    val fileType = jsonContent?.optString("fileType") ?: "pdf"
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
        timestamp = timestamp,
        fileType = fileType
    )
}

fun extractEvaluationFormData(message: Message): EvaluationFormData {
    try {
        val contentParts = message.content.split("\n", limit = 2)
        if (contentParts.size < 2) {
            return createDefaultEvaluationFormData(message)
        }

        val jsonContent = try {
            JSONObject(contentParts[1])
        } catch (e: Exception) {
            return createDefaultEvaluationFormData(message)
        }


        val id = jsonContent.optString("id", "")
        val formId = jsonContent.optString("formId", "")
        val formTitle = jsonContent.optString("formTitle", "Değerlendirme Formu")
        val score = jsonContent.optInt("score", 0)
        val maxScore = jsonContent.optInt("maxScore", 0)
        val notes = jsonContent.optString("notes", "")

        val questions = mutableMapOf<String, String>()
        try {
            val questionsJsonStr = jsonContent.optString("questions", "{}")
            if (questionsJsonStr.isNotBlank() && questionsJsonStr != "{}") {
                val questionsJson = JSONObject(questionsJsonStr)
                val keys = questionsJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    questions[key] = questionsJson.optString(key, "")
                }
            }
        } catch (e: Exception) {

        }


        val answers = mutableMapOf<String, String>()
        try {
            val answersJsonStr = jsonContent.optString("answers", "{}")
            if (answersJsonStr.isNotBlank() && answersJsonStr != "{}") {
                val answersJson = JSONObject(answersJsonStr)
                val keys = answersJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    answers[key] = answersJson.optString(key, "")
                }
            }
        } catch (e: Exception) {

        }


        val timestamp = try {
            val timeStr = jsonContent.optString("dateCompleted", "0")
            Date(timeStr.toLong())
        } catch (e: Exception) {
            message.timestamp
        }

        return EvaluationFormData(
            id = id,
            formId = formId,
            formTitle = formTitle,
            formDescription = "",
            questions = questions,
            answers = answers,
            score = score,
            maxScore = maxScore,
            timestamp = timestamp,
            notes = notes
        )
    } catch (e: Exception) {
        return createDefaultEvaluationFormData(message)
    }
}


data class EvaluationFormData(
    val id: String,
    val formId: String,
    val formTitle: String,
    val formDescription: String,
    val questions: Map<String, String>,
    val answers: Map<String, String>,
    val score: Int,
    val maxScore: Int,
    val timestamp: Date,
    val notes: String
)

private fun createDefaultEvaluationFormData(message: Message): EvaluationFormData {
    return EvaluationFormData(
        id = "",
        formId = "",
        formTitle = "Değerlendirme Formu",
        formDescription = "",
        questions = emptyMap(),
        answers = emptyMap(),
        score = 0,
        maxScore = 0,
        timestamp = message.timestamp,
        notes = ""
    )
}

data class RadiologicalImageData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val timestamp: Date,
    val fileType: String = "image"
)

data class MedicalReportData(
    val title: String,
    val description: String,
    val fileUrl: String,
    val doctorName: String = "",
    val hospitalName: String = "",
    val timestamp: Date,
    val fileType: String = "pdf" )

@Composable
fun RadiologicalImageMessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    onClick: () -> Unit
) {
    val imageData = extractRadiologicalImageData(message)
    var showFullScreenImage by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isPdf = imageData.fileType == "pdf"

    Card(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clickable {
                if (isPdf) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse(imageData.imageUrl)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "PDF açılamadı: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                        onClick()
                    }
                } else if (imageData.imageUrl.isNotEmpty()) {
                    showFullScreenImage = true
                } else {
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
        elevation = cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image,
                    contentDescription = null,
                    tint = if (isCurrentUser) Color.White.copy(alpha = 0.9f) else Color(59, 62, 104),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Radyolojik ${if (isPdf) "PDF" else "Görüntü"}",
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isCurrentUser) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {

                Text(
                    text = imageData.title,
                    fontWeight = FontWeight.Medium,
                    color = if (isCurrentUser) Color.White else Color.Black,
                    fontSize = 13.sp
                )
                if (isPdf) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else Color(59, 62, 104),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "PDF Belgesi",
                            color = if (isCurrentUser) Color.White.copy(alpha = 0.8f) else Color.DarkGray,
                            fontSize = 12.sp
                        )
                    }
                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF0F0F0).copy(alpha = if (isCurrentUser) 0.2f else 1f))
                    ) {
                        if (imageData.imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = imageData.imageUrl,
                                contentDescription = imageData.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
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
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }


                if (imageData.description.isNotEmpty()) {
                    Text(
                        text = imageData.description,
                        color = if (isCurrentUser) Color.White.copy(alpha = 0.8f)
                        else Color.Black.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(imageData.timestamp),
                    fontSize = 10.sp,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.6f)
                    else Color.Black.copy(alpha = 0.6f)
                )
            }
        }
    }


    if (showFullScreenImage && imageData.imageUrl.isNotEmpty() && !isPdf) {
        FullScreenImageViewer(
            imageUrl = imageData.imageUrl,
            title = imageData.title,
            onDismiss = { showFullScreenImage = false }
        )
    }
}


@Composable
fun MedicalReportMessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    onClick: () -> Unit
) {
    val reportData = extractMedicalReportData(message)
    val context = LocalContext.current
    var showFullScreenImage by remember { mutableStateOf(false) }
    val isPdf = reportData.fileType == "pdf"

    Card(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .clickable {
                if (isPdf) {
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
                        onClick()
                    }
                } else if (reportData.fileUrl.isNotEmpty()) {
                    showFullScreenImage = true
                } else {
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
        elevation = cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image,
                    contentDescription = null,
                    tint = if (isCurrentUser) Color.White.copy(alpha = 0.9f) else Color(59, 62, 104),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tıbbi ${if (isPdf) "Rapor" else "Görüntü"}",
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser) Color.White else Color.Black,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isCurrentUser) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {

                Text(
                    text = reportData.title,
                    fontWeight = FontWeight.Medium,
                    color = if (isCurrentUser) Color.White else Color.Black,
                    fontSize = 13.sp
                )


                if (reportData.doctorName.isNotEmpty() || reportData.hospitalName.isNotEmpty()) {
                    Text(
                        text = buildString {
                            if (reportData.doctorName.isNotEmpty()) {
                                append("Dr. ${reportData.doctorName}")
                                if (reportData.hospitalName.isNotEmpty()) {
                                    append(" • ")
                                }
                            }
                            if (reportData.hospitalName.isNotEmpty()) {
                                append(reportData.hospitalName)
                            }
                        },
                        fontSize = 11.sp,
                        color = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }


                if (isPdf) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = if (isCurrentUser) Color.White.copy(alpha = 0.7f) else Color(59, 62, 104),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "PDF Raporu",
                                color = if (isCurrentUser) Color.White.copy(alpha = 0.8f) else Color.DarkGray,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Görüntülemek için dokunun",
                                color = if (isCurrentUser) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                fontSize = 10.sp
                            )
                        }
                    }
                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .padding(vertical = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF0F0F0).copy(alpha = if (isCurrentUser) 0.2f else 1f))
                    ) {
                        if (reportData.fileUrl.isNotEmpty()) {
                            AsyncImage(
                                model = reportData.fileUrl,
                                contentDescription = reportData.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
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
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }


                if (reportData.description.isNotEmpty()) {
                    Text(
                        text = reportData.description,
                        color = if (isCurrentUser) Color.White.copy(alpha = 0.8f)
                        else Color.Black.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(reportData.timestamp),
                    fontSize = 10.sp,
                    color = if (isCurrentUser) Color.White.copy(alpha = 0.6f)
                    else Color.Black.copy(alpha = 0.6f)
                )
            }
        }
    }


    if (showFullScreenImage && reportData.fileUrl.isNotEmpty() && !isPdf) {
        FullScreenImageViewer(
            imageUrl = reportData.fileUrl,
            title = reportData.title,
            onDismiss = { showFullScreenImage = false }
        )
    }
}


@Composable
fun EvaluationFormMessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    onClick: () -> Unit
) {
    val formData = extractEvaluationFormData(message)

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
        elevation = cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = formData.formTitle,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentUser) Color.White else Color.Black,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isCurrentUser) Color.White.copy(alpha = 0.1f)
                        else Color(0xFFF0F0F0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Assignment,
                        contentDescription = "Değerlendirme Formu",
                        tint = if (isCurrentUser) Color.White.copy(alpha = 0.9f) else Color(59, 62, 104),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Değerlendirme Formu",
                        color = if (isCurrentUser) Color.White else Color(59, 62, 104),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Puan: ${formData.score}/${formData.maxScore}",
                        color = if (isCurrentUser) Color.White.copy(alpha = 0.9f) else Color(59, 62, 104),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Zaman
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(formData.timestamp),
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
    val context = LocalContext.current
    var showFullScreenImage by remember { mutableStateOf(false) }

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

                if (imageData.fileType == "pdf") {

                } else {

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable {
                                if (imageData.imageUrl.isNotEmpty()) {
                                    showFullScreenImage = true
                                }
                            }
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

    if (showFullScreenImage && imageData.imageUrl.isNotEmpty()) {
        FullScreenImageViewer(
            imageUrl = imageData.imageUrl,
            title = imageData.title,
            onDismiss = { showFullScreenImage = false }
        )
    }
}

@Composable
fun MedicalReportDetailDialog(
    message: Message,
    onDismiss: () -> Unit
) {
    val reportData = extractMedicalReportData(message)
    val context = LocalContext.current
    val isPdf = reportData.fileType == "pdf"
    var showFullScreenImage by remember { mutableStateOf(false) }

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

                if (isPdf) {

                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable {
                                if (reportData.fileUrl.isNotEmpty()) {
                                    showFullScreenImage = true
                                }
                            }
                    ) {
                        AsyncImage(
                            model = reportData.fileUrl,
                            contentDescription = reportData.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit,
                            error = ColorPainter(Color(0xFFE0E0E0)),
                            fallback = ColorPainter(Color(0xFFE0E0E0))
                        )
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

    if (showFullScreenImage && reportData.fileUrl.isNotEmpty()) {
        FullScreenImageViewer(
            imageUrl = reportData.fileUrl,
            title = reportData.title,
            onDismiss = { showFullScreenImage = false }
        )
    }
}


@Composable
fun EvaluationFormDetailDialog(
    message: Message,
    onDismiss: () -> Unit
) {
    val formData = extractEvaluationFormData(message)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formData.formTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(59, 62, 104)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat"
                        )
                    }
                }


                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(59, 62, 104, 0x10)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Score,
                                contentDescription = null,
                                tint = Color(59, 62, 104)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Puan: ${formData.score}/${formData.maxScore}",
                                fontWeight = FontWeight.Bold,
                                color = Color(59, 62, 104)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tamamlanma: ${
                                SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                    .format(formData.timestamp)
                            }",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }


                Text(
                    text = "Yanıtlar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(59, 62, 104),
                    modifier = Modifier.padding(vertical = 8.dp)
                )


                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (formData.answers.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Yanıt bulunmuyor",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {

                        formData.answers.forEach { (questionId, answer) ->
                            val questionText = formData.questions[questionId] ?: "Soru $questionId"

                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF5F5F5)
                                    ),
                                    elevation = CardDefaults.cardElevation(0.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = questionText,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(59, 62, 104)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = answer,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }


                    if (formData.notes.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Notlar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(59, 62, 104)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F5F5)
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Text(
                                    text = formData.notes,
                                    modifier = Modifier.padding(16.dp),
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }


                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
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
// Tam ekran görüntüleme için ortak bileşen
@Composable
fun FullScreenImageViewer(
    imageUrl: String,
    title: String = "",
    onDismiss: () -> Unit
) {
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
                .background(Color.Black)
        ) {

            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                error = ColorPainter(Color(0xFF333333)),
                fallback = ColorPainter(Color(0xFF333333))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.7f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 100f
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (title.isNotEmpty()) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}








