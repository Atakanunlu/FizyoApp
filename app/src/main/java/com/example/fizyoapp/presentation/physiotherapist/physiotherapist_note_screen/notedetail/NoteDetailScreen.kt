package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                        containerColor = MaterialTheme.colorScheme.error
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
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
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
                        containerColor = MaterialTheme.colorScheme.error
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
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Yeni Ek Not",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Ekler",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            if (state.tempImageUris.isNotEmpty()) {
                                Text(
                                    "Eklenecek Görseller (${state.tempImageUris.size})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
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
                                                    MaterialTheme.colorScheme.outline,
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
                                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Kaldır",
                                                    tint = MaterialTheme.colorScheme.onErrorContainer,
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
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
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
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Description,
                                                contentDescription = "Belge",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = filename,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            IconButton(
                                                onClick = { viewModel.onEvent(NoteDetailEvent.RemoveTempDocument(index)) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Kaldır",
                                                    tint = MaterialTheme.colorScheme.error,
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
                                    shape = RoundedCornerShape(8.dp)
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
                                    shape = RoundedCornerShape(8.dp)
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
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("İptal")
                        }

                        Button(
                            onClick = {
                                viewModel.onEvent(NoteDetailEvent.AddUpdate)
                                showAddUpdateDialog = false
                            },
                            enabled = state.updateText.isNotBlank()
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
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notu düzenlediğinizde güncelleme tarihi otomatik olarak değiştirilecektir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(NoteDetailEvent.SaveUpdateEdit) },
                    enabled = state.updateText.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
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
                            maxLines = 1
                        )
                        state.note?.patientName?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            "Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddUpdateDialog = true },
                icon = { Icon(Icons.Default.Add, "Not Ekle") },
                text = { Text("Ek Not") },
                expanded = scrollState.value == 0
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                state.note?.let { note ->
                    val backgroundColor = when (note.color) {
                        NoteColor.WHITE -> MaterialTheme.colorScheme.surface
                        NoteColor.LIGHT_YELLOW -> Color(0xFFFFF9C4)
                        NoteColor.ORANGE -> Color(0xFFFFE0B2)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor)
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        DateInfoCard(note, dateFormatter)
                        MainNoteCard(note)

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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ek Notlar (${note.updates.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
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
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Error,
                            null,
                            tint = MaterialTheme.colorScheme.error,
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
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
private fun DateColumn(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, date: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MainNoteCard(note: com.example.fizyoapp.domain.model.note.Note) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.Description,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Ana Not",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
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
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 8.dp)
                )

                Text(
                    text = "Görsel ve Belge Ekle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Bu nota hasta ile ilgili fotoğraflar ve belgeler ekleyebilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onAddImage,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
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
                        shape = RoundedCornerShape(8.dp)
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
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dosya Ekleri",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (images.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Görseller (${images.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
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
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.3f
                                        )
                                    )
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Belge ${index + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "PDF Belgesi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Icon(
                                    Icons.Default.OpenInNew,
                                    contentDescription = "Aç",
                                    tint = MaterialTheme.colorScheme.primary,
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
                        shape = RoundedCornerShape(8.dp)
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
                        shape = RoundedCornerShape(8.dp)
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.NoteAdd,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Henüz Ek Not Bulunmuyor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Sağ alttaki '+ Ek Not' butonuna tıklayarak hastanızla ilgili yeni bir not ekleyebilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(8.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                (index + 1).toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        dateFormatter.format(update.updateDate),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            "Düzenle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            "Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Text(
                update.updateText,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            if (update.images.isNotEmpty() || update.documents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (update.images.isNotEmpty()) {
                    Text(
                        "Görseller",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
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
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
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
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
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
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(
                                            alpha = 0.3f
                                        )
                                    )
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Belge ${docIndex + 1}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Default.OpenInNew,
                                    contentDescription = "Aç",
                                    tint = MaterialTheme.colorScheme.primary,
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
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = "Görsel Ekle",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Görsel Ekle", style = MaterialTheme.typography.bodySmall)
                }

                OutlinedButton(
                    onClick = onAddDocument,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Belge Ekle",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Belge Ekle", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}