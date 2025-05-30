package com.example.fizyoapp.presentation.user.illnessrecord.medicalrecord

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.illnesrecordscreen.medicalrecord.MedicalReport
import com.example.fizyoapp.domain.model.messagesscreen.ChatThread
import com.example.fizyoapp.presentation.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalReportScreen(
    navController: NavController,
    viewModel: MedicalReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var selectedReport by remember { mutableStateOf<MedicalReport?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    var showFileTypeDialog by remember { mutableStateOf(false) }
    var showAddPdfDialog by remember { mutableStateOf(false) }
    var showAddImageDialog by remember { mutableStateOf(false) }
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onEvent(MedicalReportEvent.FileSelected(uri, "pdf"))
            showAddPdfDialog = true
        }
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onEvent(MedicalReportEvent.FileSelected(uri, "image"))
            showAddImageDialog = true
        }
    }
    val openPdf = { pdfUrl: String ->
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(pdfUrl)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "PDF açılamadı: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tıbbi Raporlar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFileTypeDialog = true },
                containerColor = primaryColor,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tıbbi Rapor Ekle"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = primaryColor
                    )
                }
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = errorColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.onEvent(MedicalReportEvent.RefreshData) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Yenile")
                    }
                }
            } else if (state.reports.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = primaryColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Henüz bir tıbbi raporunuz bulunmuyor",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sağ alttaki + butonuna tıklayarak rapor ekleyebilirsiniz",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { showFileTypeDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tıbbi Rapor Ekle")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.reports) { report ->
                        MedicalReportCard(
                            report = report,
                            onViewClicked = {
                                selectedReport = report
                                if (report.fileType == "pdf") {
                                    openPdf(report.fileUrl)
                                }
                            },
                            onShareClicked = {
                                selectedReport = report
                                showShareDialog = true
                            },
                            onDeleteClicked = {
                                viewModel.onEvent(MedicalReportEvent.DeleteReport(report.fileUrl))
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
            if (state.actionError != null) {
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.onEvent(MedicalReportEvent.DismissError) }) {
                            Text("TAMAM", color = Color.White)
                        }
                    },
                    containerColor = errorColor,
                    contentColor = Color.White
                ) {
                    Text(state.actionError!!)
                }
            }
            AnimatedVisibility(
                visible = state.successMessage != null,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = fadeOut() + slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ),
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = successColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = state.successMessage ?: "",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
    if (showFileTypeDialog) {
        AlertDialog(
            onDismissRequest = { showFileTypeDialog = false },
            title = { Text("Tıbbi Rapor Ekle") },
            text = { Text("Eklemek istediğiniz dosya türünü seçin") },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            pdfPickerLauncher.launch("application/pdf")
                            showFileTypeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PDF Yükle")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                            showFileTypeDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Görüntü"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Görüntü Yükle")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = { showFileTypeDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("İptal")
                    }
                }
            },
            dismissButton = null
        )
    }
    if (selectedReport != null && selectedReport!!.fileType == "image" && !showShareDialog) {
        Dialog(onDismissRequest = { selectedReport = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor
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
                            text = selectedReport!!.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { selectedReport = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat"
                            )
                        }
                    }
                    Text(
                        text = selectedReport!!.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    if (selectedReport!!.doctorName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Doktor: ${selectedReport!!.doctorName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                    if (selectedReport!!.hospitalName.isNotBlank()) {
                        Text(
                            text = "Hastane: ${selectedReport!!.hospitalName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                    Text(
                        text = "Tarih: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(selectedReport!!.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(backgroundColor.copy(alpha = 0.6f))
                    ) {
                        AsyncImage(
                            model = selectedReport!!.fileUrl,
                            contentDescription = selectedReport!!.title,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp),
                            contentScale = ContentScale.Fit,
                            error = ColorPainter(backgroundColor.copy(alpha = 0.8f)),
                            fallback = ColorPainter(backgroundColor.copy(alpha = 0.8f))
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { selectedReport = null }
                        ) {
                            Text("Kapat")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showShareDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Paylaş"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Paylaş")
                        }
                    }
                }
            }
        }
    }
    if (selectedReport != null && selectedReport!!.fileType == "pdf" && !showShareDialog) {
        Dialog(onDismissRequest = { selectedReport = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = surfaceColor
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
                            text = selectedReport!!.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { selectedReport = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat"
                            )
                        }
                    }
                    Text(
                        text = selectedReport!!.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    if (selectedReport!!.doctorName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Doktor: ${selectedReport!!.doctorName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                    if (selectedReport!!.hospitalName.isNotBlank()) {
                        Text(
                            text = "Hastane: ${selectedReport!!.hospitalName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                    Text(
                        text = "Tarih: ${SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(selectedReport!!.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(backgroundColor.copy(alpha = 0.6f))
                            .border(1.dp, cardBorderColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "PDF Rapor",
                                style = MaterialTheme.typography.titleMedium,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    openPdf(selectedReport!!.fileUrl)
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryColor
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Görüntüle"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Görüntüle")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { selectedReport = null }
                        ) {
                            Text("Kapat")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showShareDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Paylaş"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Paylaş")
                        }
                    }
                }
            }
        }
    }
    if (showShareDialog && selectedReport != null) {
        ShareDialog(
            threads = state.recentThreads,
            currentUserId = state.currentUserId,
            onDismiss = {
                showShareDialog = false
                selectedReport = null
            },
            onShareClicked = { userId ->
                viewModel.onEvent(
                    MedicalReportEvent.ShareReport(
                        selectedReport!!.id,
                        userId
                    )
                )
                showShareDialog = false
                selectedReport = null
            }
        )
    }
    if (showAddPdfDialog) {
        AddMedicalReportDialog(
            onDismiss = { showAddPdfDialog = false },
            onConfirm = { title, description, doctorName, hospitalName ->
                viewModel.onEvent(
                    MedicalReportEvent.AddReport(
                        title = title,
                        description = description,
                        doctorName = doctorName,
                        hospitalName = hospitalName,
                        fileType = "pdf"
                    )
                )
                showAddPdfDialog = false
            },
            dialogTitle = "PDF Tıbbi Rapor Ekle",
            icon = Icons.Default.PictureAsPdf
        )
    }
    if (showAddImageDialog) {
        AddMedicalReportDialog(
            onDismiss = { showAddImageDialog = false },
            onConfirm = { title, description, doctorName, hospitalName ->
                viewModel.onEvent(
                    MedicalReportEvent.AddReport(
                        title = title,
                        description = description,
                        doctorName = doctorName,
                        hospitalName = hospitalName,
                        fileType = "image"
                    )
                )
                showAddImageDialog = false
            },
            dialogTitle = "Görüntü Tıbbi Rapor Ekle",
            icon = Icons.Default.Image
        )
    }
}

@Composable
fun MedicalReportCard(
    report: MedicalReport,
    onViewClicked: () -> Unit,
    onShareClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        )
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
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (report.fileType == "image") {
                        AsyncImage(
                            model = report.thumbnailUrl,
                            contentDescription = report.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = ColorPainter(backgroundColor.copy(alpha = 0.8f)),
                            fallback = ColorPainter(backgroundColor.copy(alpha = 0.8f))
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Assignment,
                            contentDescription = "Rapor",
                            tint = primaryColor,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = report.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = report.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (report.doctorName.isNotBlank() || report.hospitalName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildString {
                                if (report.doctorName.isNotBlank()) {
                                    append("Dr. ${report.doctorName}")
                                    if (report.hospitalName.isNotBlank()) {
                                        append(" • ")
                                    }
                                }
                                if (report.hospitalName.isNotBlank()) {
                                    append(report.hospitalName)
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(report.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = textColor.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (report.fileType == "pdf") "PDF" else "Görüntü",
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                IconButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = errorColor.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onViewClicked,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    ),
                    border = BorderStroke(1.dp, primaryColor)
                ) {
                    Icon(
                        imageVector = if (report.fileType == "pdf") Icons.Default.OpenInNew else Icons.Default.Visibility,
                        contentDescription = "Görüntüle"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Görüntüle")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onShareClicked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Paylaş"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paylaş")
                }
            }
        }
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Tıbbi Raporu Sil") },
            text = { Text("Bu tıbbi raporu silmek istediğinize emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClicked()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = errorColor
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
fun AddMedicalReportDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, doctorName: String, hospitalName: String) -> Unit,
    dialogTitle: String,
    icon: ImageVector
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf<String?>(null) }
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = surfaceColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dialogTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = if (it.isBlank()) "Başlık boş olamaz" else null
                    },
                    label = { Text("Başlık") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = titleError != null,
                    supportingText = {
                        if (titleError != null) {
                            Text(titleError!!)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama (İsteğe Bağlı)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = doctorName,
                    onValueChange = { doctorName = it },
                    label = { Text("Doktor Adı (İsteğe Bağlı)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = hospitalName,
                    onValueChange = { hospitalName = it },
                    label = { Text("Hastane / Klinik Adı (İsteğe Bağlı)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = "Başlık boş olamaz"
                                return@Button
                            }
                            onConfirm(title, description, doctorName, hospitalName)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun ShareDialog(
    threads: List<ChatThread>,
    currentUserId: String,
    onDismiss: () -> Unit,
    onShareClicked: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = surfaceColor
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Tıbbi Rapor Paylaş",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bu raporu paylaşmak istediğiniz kişiyi seçin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (threads.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz bir konuşmanız bulunmuyor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(threads) { thread ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val otherUserId = thread.participantIds.firstOrNull {
                                            it != currentUserId
                                        } ?: return@clickable
                                        onShareClicked(otherUserId)
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = backgroundColor.copy(alpha = 0.6f)
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(primaryColor.copy(alpha = 0.2f))
                                    ) {
                                        if (thread.otherParticipantPhotoUrl.isNotEmpty()) {
                                            AsyncImage(
                                                model = thread.otherParticipantPhotoUrl,
                                                contentDescription = "Profil fotoğrafı",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Profil",
                                                tint = primaryColor,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(8.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = thread.otherParticipantName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss
                    ) {
                        Text("İptal")
                    }
                }
            }
        }
    }
}