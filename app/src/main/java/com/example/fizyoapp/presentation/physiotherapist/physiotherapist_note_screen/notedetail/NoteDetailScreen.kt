package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.note.NoteColor
import com.example.fizyoapp.domain.model.note.NoteUpdate
import com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.NoteFullScreenImageViewer
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Locale

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val textColor = Color.DarkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavController,
    noteId: String,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale.getDefault())
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddUpdateDialog by remember { mutableStateOf(false) }
    var showDeleteUpdateDialog by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    var showFullScreenImage by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }

    if (showFullScreenImage) {
        Dialog(
            onDismissRequest = { showFullScreenImage = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            NoteFullScreenImageViewer(
                imageUrl = selectedImageUrl,
                onDismiss = { showFullScreenImage = false }
            )
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(NoteDetailEvent.AddImage(it))
        }
    }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(NoteDetailEvent.AddDocument(it))
        }
    }

    val updateImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(NoteDetailEvent.AddImageToUpdate(it))
        }
    }

    val updateDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(NoteDetailEvent.AddDocumentToUpdate(it))
        }
    }

    LaunchedEffect(state.showImagePicker) {
        if (state.showImagePicker) {
            imageLauncher.launch("image/*")
        }
    }

    LaunchedEffect(state.showDocumentPicker) {
        if (state.showDocumentPicker) {
            documentLauncher.launch("application/pdf")
        }
    }

    LaunchedEffect(state.showUpdateImagePicker) {
        if (state.showUpdateImagePicker) {
            updateImageLauncher.launch("image/*")
        }
    }

    LaunchedEffect(state.showUpdateDocumentPicker) {
        if (state.showUpdateDocumentPicker) {
            updateDocumentLauncher.launch("application/pdf")
        }
    }

    LaunchedEffect(true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is NoteDetailViewModel.UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Notu Sil") },
            text = { Text("Bu notu ve tüm ek notlarını silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(NoteDetailEvent.DeleteNote)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB71C1C)
                    )
                ) {
                    Icon(Icons.Default.Delete, null, Modifier.padding(end = 8.dp))
                    Text("Sil")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            },
            icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFB71C1C)) },
            shape = RoundedCornerShape(16.dp)
        )
    }

    showDeleteUpdateDialog?.let { index ->
        AlertDialog(
            onDismissRequest = { showDeleteUpdateDialog = null },
            title = { Text("Ek Notu Sil") },
            text = { Text("Bu ek notu silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(NoteDetailEvent.DeleteUpdate(index))
                        showDeleteUpdateDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB71C1C)
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteUpdateDialog = null }) {
                    Text("İptal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showAddUpdateDialog) {
        Dialog(
            onDismissRequest = { showAddUpdateDialog = false },
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = surfaceColor
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Yeni Ek Not",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = state.updateText,
                        onValueChange = { viewModel.onEvent(NoteDetailEvent.UpdateTextChanged(it)) },
                        label = { Text("Not İçeriği") },
                        placeholder = { Text("Bu hasta için ek notunuzu buraya yazabilirsiniz...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = primaryColor.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Ekler",
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            if (state.tempImageUris.isNotEmpty()) {
                                Text(
                                    "Eklenecek Görseller (${state.tempImageUris.size})",
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(state.tempImageUris.size) { index ->
                                        val uri = state.tempImageUris[index]
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    1.dp,
                                                    primaryColor.copy(alpha = 0.3f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                        ) {
                                            AsyncImage(
                                                model = uri,
                                                contentDescription = "Seçilen görsel",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            IconButton(
                                                onClick = { viewModel.onEvent(NoteDetailEvent.RemoveTempImage(index)) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(24.dp)
                                                    .background(
                                                        Color(0xFFB71C1C).copy(alpha = 0.7f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Kaldır",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            if (state.tempDocumentUris.isNotEmpty()) {
                                Text(
                                    "Eklenecek Belgeler (${state.tempDocumentUris.size})",
                                    fontWeight = FontWeight.Medium,
                                    color = textColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    state.tempDocumentUris.forEachIndexed { index, uri ->
                                        val filename = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                                            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                            cursor.moveToFirst()
                                            cursor.getString(nameIndex)
                                        } ?: "Belge"
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(primaryColor.copy(alpha = 0.05f))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Description,
                                                contentDescription = "Belge",
                                                tint = primaryColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = filename,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = textColor
                                            )
                                            IconButton(
                                                onClick = { viewModel.onEvent(NoteDetailEvent.RemoveTempDocument(index)) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Kaldır",
                                                    tint = Color(0xFFB71C1C),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.onEvent(NoteDetailEvent.ShowUpdateImagePicker) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryColor
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Görsel Ekle",
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text("Görsel Ekle")
                                }
                                Button(
                                    onClick = { viewModel.onEvent(NoteDetailEvent.ShowUpdateDocumentPicker) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryColor
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = "Belge Ekle",
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Text("Belge Ekle")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showAddUpdateDialog = false },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = primaryColor
                            )
                        ) {
                            Text("İptal")
                        }
                        Button(
                            onClick = {
                                viewModel.onEvent(NoteDetailEvent.AddUpdate)
                                showAddUpdateDialog = false
                            },
                            enabled = state.updateText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("Ekle")
                        }
                    }
                }
            }
        }
    }

    if (state.isEditingUpdate) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(NoteDetailEvent.CancelUpdateEdit) },
            title = { Text("Notu Düzenle") },
            text = {
                Column {
                    OutlinedTextField(
                        value = state.updateText,
                        onValueChange = { viewModel.onEvent(NoteDetailEvent.UpdateTextChanged(it)) },
                        label = { Text("Not İçeriği") },
                        placeholder = { Text("Bu hasta için ek notunuzu buraya yazabilirsiniz...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notu düzenlediğinizde güncelleme tarihi otomatik olarak değiştirilecektir.",
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(NoteDetailEvent.SaveUpdateEdit) },
                    enabled = state.updateText.isNotBlank(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    )
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.onEvent(NoteDetailEvent.CancelUpdateEdit) },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("İptal")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.note?.title ?: "Not Detayı",
                            maxLines = 1,
                            color = Color.White
                        )
                        state.note?.patientName?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Geri",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            "Sil",
                            tint = Color.White
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
            ExtendedFloatingActionButton(
                onClick = { showAddUpdateDialog = true },
                icon = { Icon(Icons.Default.Add, "Not Ekle") },
                text = { Text("Ek Not") },
                expanded = scrollState.value == 0,
                containerColor = primaryColor,
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryColor,
                    strokeWidth = 4.dp
                )
            } else {
                state.note?.let { note ->
                    val noteBackgroundColor = when (note.color) {
                        NoteColor.WHITE -> surfaceColor
                        NoteColor.LIGHT_YELLOW -> Color(0xFFFFF9C4)
                        NoteColor.ORANGE -> Color(0xFFFFE0B2)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        DateInfoCard(note, dateFormatter)
                        MainNoteCard(note, noteBackgroundColor)
                        AttachmentsCard(
                            images = note.images,
                            documents = note.documents,
                            onAddImage = { viewModel.onEvent(NoteDetailEvent.ShowImagePicker) },
                            onAddDocument = { viewModel.onEvent(NoteDetailEvent.ShowDocumentPicker) },
                            onImageClick = { url ->
                                selectedImageUrl = url
                                showFullScreenImage = true
                            },
                            context = context
                        )
                        if (note.updates.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp, top = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.List,
                                    null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ek Notlar (${note.updates.size})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                            NoteUpdatesList(
                                updates = note.updates,
                                dateFormatter = dateFormatter,
                                onEdit = { viewModel.onEvent(NoteDetailEvent.EditUpdate(it)) },
                                onDelete = { showDeleteUpdateDialog = it },
                                onAddImage = { viewModel.onEvent(NoteDetailEvent.ShowUpdateImagePicker) },
                                onAddDocument = { viewModel.onEvent(NoteDetailEvent.ShowUpdateDocumentPicker) },
                                onImageClick = { url ->
                                    selectedImageUrl = url
                                    showFullScreenImage = true
                                },
                                context = context
                            )
                        } else {
                            EmptyUpdatesCard { showAddUpdateDialog = true }
                        }
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFFB71C1C),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Error,
                            null,
                            tint = Color.White,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = state.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun DateInfoCard(note: com.example.fizyoapp.domain.model.note.Note, dateFormatter: SimpleDateFormat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DateColumn(
                icon = Icons.Default.DateRange,
                title = "OLUŞTURULMA",
                date = dateFormatter.format(note.creationDate)
            )
            HorizontalDivider(
                modifier = Modifier
                    .height(36.dp)
                    .width(1.dp),
                color = textColor.copy(alpha = 0.1f)
            )
            DateColumn(
                icon = Icons.Default.Update,
                title = "SON GÜNCELLEME",
                date = dateFormatter.format(note.updateDate)
            )
        }
    }
}

@Composable
private fun DateColumn(icon: ImageVector, title: String, date: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon, null,
            tint = primaryColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = textColor.copy(alpha = 0.6f)
        )
        Text(
            text = date,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun MainNoteCard(note: com.example.fizyoapp.domain.model.note.Note, backgroundColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.Description,
                    null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Ana Not",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
            Text(
                text = note.content,
                modifier = Modifier.padding(vertical = 8.dp),
                color = textColor
            )
        }
    }
}

@Composable
fun AttachmentsCard(
    images: List<String>,
    documents: List<String>,
    onAddImage: () -> Unit,
    onAddDocument: () -> Unit,
    onImageClick: (String) -> Unit,
    context: Context
) {
    if (images.isEmpty() && documents.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = primaryColor.copy(alpha = 0.1f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 8.dp)
                )
                Text(
                    text = "Görsel ve Belge Ekle",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bu nota hasta ile ilgili fotoğraflar ve belgeler ekleyebilirsiniz.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = textColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onAddImage,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Görsel Ekle",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Görsel Ekle")
                    }
                    Button(
                        onClick = onAddDocument,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Belge Ekle",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Belge Ekle")
                    }
                }
            }
        }
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = surfaceColor
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dosya Ekleri",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
                if (images.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Görseller (${images.size})",
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(images.size) { index ->
                            val imageUrl = images[index]
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.dp,
                                        primaryColor.copy(alpha = 0.3f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onImageClick(imageUrl)
                                    }
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Ek görsel",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
                if (documents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Belgeler (${documents.size})",
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        documents.forEachIndexed { index, documentUrl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.dp,
                                        primaryColor.copy(alpha = 0.3f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .background(primaryColor.copy(alpha = 0.05f))
                                    .padding(12.dp)
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse(documentUrl)
                                        context.startActivity(intent)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "Belge",
                                    tint = primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Belge ${index + 1}",
                                        fontWeight = FontWeight.Medium,
                                        color = textColor
                                    )
                                    Text(
                                        text = "PDF Belgesi",
                                        color = textColor.copy(alpha = 0.6f)
                                    )
                                }
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Aç",
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddImage,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = "Görsel Ekle",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Görsel Ekle")
                    }
                    Button(
                        onClick = onAddDocument,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Belge Ekle",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Belge Ekle")
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteUpdatesList(
    updates: List<NoteUpdate>,
    dateFormatter: SimpleDateFormat,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    onAddImage: (Int) -> Unit,
    onAddDocument: (Int) -> Unit,
    onImageClick: (String) -> Unit,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(primaryColor.copy(alpha = 0.05f))
            .padding(12.dp)
    ) {
        updates.forEachIndexed { index, update ->
            NoteUpdateCard(
                update = update,
                index = index,
                dateFormatter = dateFormatter,
                onEdit = { onEdit(index) },
                onDelete = { onDelete(index) },
                onAddImage = { onAddImage(index) },
                onAddDocument = { onAddDocument(index) },
                onImageClick = onImageClick,
                context = context
            )
        }
    }
}

@Composable
private fun EmptyUpdatesCard(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = surfaceColor
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.NoteAdd,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = primaryColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Henüz Ek Not Bulunmuyor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Sağ alttaki '+ Ek Not' butonuna tıklayarak hastanızla ilgili yeni bir not ekleyebilirsiniz.",
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("İlk Ek Notu Ekle")
                }
            }
        }
    }
}

@Composable
fun NoteUpdateCard(
    update: NoteUpdate,
    index: Int,
    dateFormatter: SimpleDateFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddImage: () -> Unit,
    onAddDocument: () -> Unit,
    onImageClick: (String) -> Unit,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        color = primaryColor.copy(alpha = 0.2f),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                (index + 1).toString(),
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        dateFormatter.format(update.updateDate),
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            "Düzenle",
                            tint = primaryColor
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            "Sil",
                            tint = Color(0xFFB71C1C)
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = primaryColor.copy(alpha = 0.1f)
            )
            Text(
                update.updateText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = textColor
            )

            if (update.images.isNotEmpty() || update.documents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = primaryColor.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(12.dp))

                if (update.images.isNotEmpty()) {
                    Text(
                        "Görseller",
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(update.images.size) { imageIndex ->
                            val imageUrl = update.images[imageIndex]
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.dp,
                                        primaryColor.copy(alpha = 0.3f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onImageClick(imageUrl)
                                    }
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Ek görsel",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                if (update.documents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Belgeler",
                        fontWeight = FontWeight.Medium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        update.documents.forEachIndexed { docIndex, documentUrl ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        primaryColor.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(primaryColor.copy(alpha = 0.05f))
                                    .padding(8.dp)
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_VIEW)
                                        intent.data = Uri.parse(documentUrl)
                                        context.startActivity(intent)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "Belge",
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Belge ${docIndex + 1}",
                                    modifier = Modifier.weight(1f),
                                    color = textColor
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Aç",
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddImage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, primaryColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = "Görsel Ekle",
                        modifier = Modifier.size(16.dp),
                        tint = primaryColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Görsel Ekle", color = primaryColor)
                }
                OutlinedButton(
                    onClick = onAddDocument,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, primaryColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Belge Ekle",
                        modifier = Modifier.size(16.dp),
                        tint = primaryColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Belge Ekle", color = primaryColor)
                }
            }
        }
    }
}

@Composable
fun NoteFullScreenImageViewer(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Tam ekran görsel",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = Color.White
                )
            }
        }
    }
}