package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.addnote

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.domain.model.note.NoteColor
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

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
                                placeholder = { Text("Örn: Ahmet Yılmaz") },
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