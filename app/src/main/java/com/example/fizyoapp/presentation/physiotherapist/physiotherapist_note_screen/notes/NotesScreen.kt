package com.example.fizyoapp.presentation.physiotherapist.physiotherapist_note_screen.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.domain.model.note.Note
import com.example.fizyoapp.domain.model.note.NoteColor
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavController,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val gridState = rememberLazyGridState()
    val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

    LaunchedEffect(true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is NotesViewModel.UiEvent.NavigateToAddNote -> {
                    navController.navigate(AppScreens.AddNoteScreen.route)
                }
                is NotesViewModel.UiEvent.NavigateToNoteDetail -> {
                    navController.navigate("note_detail_screen/${event.noteId}")
                }
            }
        }
    }

    LaunchedEffect(navController) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<Boolean>("refresh_notes")?.observe(navController.currentBackStackEntry!!) { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.onEvent(NotesEvent.Refresh)
                savedStateHandle.set("refresh_notes", false)
            }
        }
    }

    DisposableEffect(navController) {
        val callback = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.route == AppScreens.NotesScreen.route) {
                viewModel.onEvent(NotesEvent.Refresh)
            }
        }
        navController.addOnDestinationChangedListener(callback)
        onDispose {
            navController.removeOnDestinationChangedListener(callback)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasta Notlarım") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onEvent(NotesEvent.NavigateToAddNote) },
                icon = { Icon(Icons.Default.Add, "Not Ekle") },
                text = { Text("Yeni Not") },
                expanded = !gridState.canScrollForward || gridState.firstVisibleItemIndex == 0,
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
            } else if (state.notes.isEmpty()) {
                EmptyNotesContent { viewModel.onEvent(NotesEvent.NavigateToAddNote) }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.notes,
                        key = { it.id }
                    ) { note ->
                        NotesScreenNoteCard(
                            note = note,
                            dateFormatter = dateFormatter,
                            onClick = { viewModel.onEvent(NotesEvent.NavigateToNoteDetail(note.id)) }
                        )
                    }
                }
            }
            state.error?.let {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = errorColor,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = it)
                }
            }
        }
    }
}

@Composable
fun EmptyNotesContent(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.Notes,
            null,
            modifier = Modifier.size(80.dp),
            tint = primaryColor.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Henüz Not Eklenmemiş",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = primaryColor
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hasta notlarınızı eklemek için sağ alttaki 'Yeni Not' butonuna tıklayın.",
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddClick,
            modifier = Modifier.padding(horizontal = 32.dp),
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
            Text("İlk Notu Ekle")
        }
    }
}

@Composable
fun NotesScreenNoteCard(
    note: Note,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit
) {
    val backgroundColor = when (note.color) {
        NoteColor.WHITE -> noteWhiteColor
        NoteColor.LIGHT_YELLOW -> noteLightYellowColor
        NoteColor.ORANGE -> noteOrangeColor
    }
    val borderColor = when (note.color) {
        NoteColor.WHITE -> cardBorderColor
        NoteColor.LIGHT_YELLOW -> noteLightYellowColor.copy(alpha = 0.5f)
        NoteColor.ORANGE -> noteOrangeColor.copy(alpha = 0.5f)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = note.patientName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = primaryColor
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = note.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 8.dp),
                color = textColor
            )
            Text(
                text = note.content,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 12.dp),
                color = textColor.copy(alpha = 0.8f)
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Son güncelleme: ${dateFormatter.format(note.updateDate)}",
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
                if (note.updates.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${note.updates.size} ek not",
                        fontSize = 12.sp,
                        color = primaryColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}