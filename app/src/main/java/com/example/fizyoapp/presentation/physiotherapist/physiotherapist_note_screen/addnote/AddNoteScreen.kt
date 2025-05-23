package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.addnote

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.note.NoteColor
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    navController: NavController,
    onBackWithRefresh: () -> Unit = {},
    viewModel: AddNoteViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val scrollState = rememberScrollState()
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val currentDate = remember { Date() }
    val context = LocalContext.current

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(AddNoteEvent.AddImage(it))
        }
    }

    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(AddNoteEvent.AddDocument(it))
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

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AddNoteViewModel.UiEvent.NavigateBack -> {
                    if (event.needsRefresh) {
                        onBackWithRefresh()
                    }
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Hasta Notu Ekle") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(AddNoteEvent.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                }
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    // Hasta Bilgileri Kartı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hasta Bilgileri",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = state.patientName,
                                onValueChange = { viewModel.onEvent(AddNoteEvent.PatientNameChanged(it)) },
                                label = { Text("Hasta Adı Soyadı") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Person, "Hasta") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = dateFormatter.format(currentDate),
                                onValueChange = { },
                                label = { Text("Başlangıç Tarihi") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.CalendarToday, "Tarih") },
                                enabled = false,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Not Bilgileri Kartı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Not Bilgileri",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = state.title,
                                onValueChange = { viewModel.onEvent(AddNoteEvent.TitleChanged(it)) },
                                label = { Text("Not Başlığı") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Örn: İlk Muayene") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Not Rengi",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                NoteColorOptions(state.noteColor, viewModel)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = state.content,
                                onValueChange = { viewModel.onEvent(AddNoteEvent.ContentChanged(it)) },
                                label = { Text("Not İçeriği") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                placeholder = { Text("Hastaya ait notları buraya yazabilirsiniz...") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Dosya Ekleme Kartı - Daha Belirgin
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dosya Ekle",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "Hasta ile ilgili görsel ve belgeleri buradan ekleyebilirsiniz.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // Görseller Bölümü
                            Text(
                                text = "Görseller",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.imageUris.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    items(state.imageUris.size) { index ->
                                        val uri = state.imageUris[index]
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
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
                                                onClick = { viewModel.onEvent(AddNoteEvent.RemoveImage(index)) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(32.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Kaldır",
                                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                                .clickable { viewModel.onEvent(AddNoteEvent.ShowImagePicker) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.AddPhotoAlternate,
                                                    contentDescription = "Görsel Ekle",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Görsel Ekle",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.onEvent(AddNoteEvent.ShowImagePicker) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = "Görsel Ekle",
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("Görsel Ekle")
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Belgeler Bölümü
                            Text(
                                text = "Belgeler",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.documentUris.isNotEmpty()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    state.documentUris.forEachIndexed { index, uri ->
                                        DocumentItem(
                                            uri = uri,
                                            onRemove = { viewModel.onEvent(AddNoteEvent.RemoveDocument(index)) }
                                        )
                                    }

                                    Button(
                                        onClick = { viewModel.onEvent(AddNoteEvent.ShowDocumentPicker) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AttachFile,
                                            contentDescription = "Belge Ekle",
                                            modifier = Modifier.padding(end = 8.dp)
                                        )
                                        Text("Başka Belge Ekle")
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.onEvent(AddNoteEvent.ShowDocumentPicker) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
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

                    Button(
                        onClick = { viewModel.onEvent(AddNoteEvent.SaveNote) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.isLoading &&
                                state.patientName.isNotBlank() &&
                                state.title.isNotBlank() &&
                                state.content.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Notu Kaydet",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.error != null) {
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteColorOptions(selectedColor: NoteColor, viewModel: AddNoteViewModel) {
    ColorOption(
        color = androidx.compose.ui.graphics.Color.White,
        selected = selectedColor == NoteColor.WHITE,
        label = "Beyaz",
        onSelect = { viewModel.onEvent(AddNoteEvent.ColorChanged(NoteColor.WHITE)) }
    )
    ColorOption(
        color = androidx.compose.ui.graphics.Color(0xFFFFF9C4),
        selected = selectedColor == NoteColor.LIGHT_YELLOW,
        label = "Sarı",
        onSelect = { viewModel.onEvent(AddNoteEvent.ColorChanged(NoteColor.LIGHT_YELLOW)) }
    )
    ColorOption(
        color = androidx.compose.ui.graphics.Color(0xFFFFE0B2),
        selected = selectedColor == NoteColor.ORANGE,
        label = "Turuncu",
        onSelect = { viewModel.onEvent(AddNoteEvent.ColorChanged(NoteColor.ORANGE)) }
    )
}

@Composable
fun ColorOption(
    color: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    label: String,
    onSelect: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
                .clickable(onClick = onSelect)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DocumentItem(uri: Uri, onRemove: () -> Unit) {
    val context = LocalContext.current
    val filename = remember {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "Belge"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                    text = filename,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "PDF Belgesi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Kaldır",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}