package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.addnote

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.note.NoteColor
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val textColor = Color.DarkGray

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

    // Dosya seçicileri tanımla
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hasta Bilgileri",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = state.patientName,
                                onValueChange = { viewModel.onEvent(AddNoteEvent.PatientNameChanged(it)) },
                                label = { Text("Hasta Adı Soyadı") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        "Hasta",
                                        tint = primaryColor
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    focusedLabelColor = primaryColor,
                                    cursorColor = primaryColor
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = dateFormatter.format(currentDate),
                                onValueChange = { },
                                label = { Text("Başlangıç Tarihi") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        "Tarih",
                                        tint = primaryColor
                                    )
                                },
                                enabled = false,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = primaryColor.copy(alpha = 0.5f),
                                    disabledLabelColor = primaryColor.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Not Bilgileri",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = state.title,
                                onValueChange = { viewModel.onEvent(AddNoteEvent.TitleChanged(it)) },
                                label = { Text("Not Başlığı") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Örn: İlk Muayene") },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    focusedLabelColor = primaryColor,
                                    cursorColor = primaryColor
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Not Rengi",
                                fontWeight = FontWeight.Medium,
                                color = textColor
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
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    focusedLabelColor = primaryColor,
                                    cursorColor = primaryColor
                                )
                            )
                        }
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = primaryColor.copy(alpha = 0.1f)
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
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Dosya Ekle",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )
                            }
                            Text(
                                text = "Hasta ile ilgili görsel ve belgeleri buradan ekleyebilirsiniz.",
                                color = textColor
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Görseller",
                                fontWeight = FontWeight.Medium,
                                color = primaryColor
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
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(
                                                    1.dp,
                                                    primaryColor.copy(alpha = 0.3f),
                                                    RoundedCornerShape(12.dp)
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
                                                        Color(0xFFB71C1C).copy(alpha = 0.7f),
                                                        CircleShape
                                                    )
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Kaldır",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    }
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .border(
                                                    1.dp,
                                                    primaryColor,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .background(primaryColor.copy(alpha = 0.1f))
                                                .clickable { imageLauncher.launch("image/*") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.AddPhotoAlternate,
                                                    contentDescription = "Görsel Ekle",
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Görsel Ekle",
                                                    color = primaryColor
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { imageLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
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
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "Belgeler",
                                fontWeight = FontWeight.Medium,
                                color = primaryColor
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
                                        onClick = { documentLauncher.launch("application/pdf") },
                                        modifier = Modifier.fillMaxWidth(),
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
                                        Text("Başka Belge Ekle")
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { documentLauncher.launch("application/pdf") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
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
                    Button(
                        onClick = { viewModel.onEvent(AddNoteEvent.SaveNote) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !state.isLoading &&
                                state.patientName.isNotBlank() &&
                                state.title.isNotBlank() &&
                                state.content.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            disabledContainerColor = primaryColor.copy(alpha = 0.5f)
                        )
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White
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
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFEBEE)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color(0xFFB71C1C)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = state.error,
                                    color = Color(0xFFB71C1C)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteColorOptions(selectedColor: NoteColor, viewModel: AddNoteViewModel) {
    ColorOption(
        color = Color.White,
        selected = selectedColor == NoteColor.WHITE,
        label = "Beyaz",
        onSelect = { viewModel.onEvent(AddNoteEvent.ColorChanged(NoteColor.WHITE)) }
    )
    ColorOption(
        color = Color(0xFFFFF9C4),
        selected = selectedColor == NoteColor.LIGHT_YELLOW,
        label = "Sarı",
        onSelect = { viewModel.onEvent(AddNoteEvent.ColorChanged(NoteColor.LIGHT_YELLOW)) }
    )
    ColorOption(
        color = Color(0xFFFFE0B2),
        selected = selectedColor == NoteColor.ORANGE,
        label = "Turuncu",
        onSelect = { viewModel.onEvent(AddNoteEvent.ColorChanged(NoteColor.ORANGE)) }
    )
}

@Composable
fun ColorOption(
    color: Color,
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
                    color = if (selected) primaryColor else Color.LightGray,
                    shape = CircleShape
                )
                .clickable(onClick = onSelect)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) primaryColor else textColor
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
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
        color = surfaceColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
                    text = filename,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "PDF Belgesi",
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Kaldır",
                    tint = Color(0xFFB71C1C)
                )
            }
        }
    }
}