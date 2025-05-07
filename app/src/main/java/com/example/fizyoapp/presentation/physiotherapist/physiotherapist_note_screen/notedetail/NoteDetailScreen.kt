package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.domain.model.note.NoteColor
import com.example.fizyoapp.domain.model.note.NoteUpdate
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

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

    if (showAddUpdateDialog || state.isEditingUpdate) {
        AlertDialog(
            onDismissRequest = {
                if (state.isEditingUpdate) {
                    viewModel.onEvent(NoteDetailEvent.CancelUpdateEdit)
                } else {
                    showAddUpdateDialog = false
                }
            },
            title = { Text(if (state.isEditingUpdate) "Notu Düzenle" else "Yeni Ek Not") },
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
                    if (state.isEditingUpdate) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Notu düzenlediğinizde güncelleme tarihi otomatik olarak değiştirilecektir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (state.isEditingUpdate) {
                            viewModel.onEvent(NoteDetailEvent.SaveUpdateEdit)
                        } else {
                            viewModel.onEvent(NoteDetailEvent.AddUpdate)
                            showAddUpdateDialog = false
                        }
                    },
                    enabled = state.updateText.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (state.isEditingUpdate) "Kaydet" else "Ekle")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        if (state.isEditingUpdate) {
                            viewModel.onEvent(NoteDetailEvent.CancelUpdateEdit)
                        } else {
                            showAddUpdateDialog = false
                        }
                    },
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

                        if (note.updates.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
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
                                onDelete = { showDeleteUpdateDialog = it }
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
private fun NoteUpdatesList(
    updates: List<NoteUpdate>,
    dateFormatter: SimpleDateFormat,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit
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
                onDelete = { onDelete(index) }
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
    onDelete: () -> Unit
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
        }
    }
}