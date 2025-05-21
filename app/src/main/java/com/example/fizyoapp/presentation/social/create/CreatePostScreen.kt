package com.example.fizyoapp.presentation.social.create

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CreatePostViewModel.CreatePostUiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(CreatePostEvent.AddMedia(it.toString()))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Gönderi") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(CreatePostEvent.CancelPost) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "İptal Et"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onEvent(CreatePostEvent.PublishPost) },
                        enabled = !state.isLoading && state.content.isNotBlank()
                    ) {
                        Text(
                            text = "Paylaş",
                            color = if (!state.isLoading && state.content.isNotBlank())
                                Color.White
                            else
                                Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Content input field
            OutlinedTextField(
                value = state.content,
                onValueChange = { viewModel.onEvent(CreatePostEvent.ContentChanged(it)) },
                placeholder = { Text("Gönderi içeriğinizi buraya yazın...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Media selection button
            Button(
                onClick = { photoLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "Fotoğraf Ekle",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Fotoğraf Ekle")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display selected media
            if (state.mediaUris.isNotEmpty()) {
                Text(
                    text = "Seçilen Medya:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(state.mediaUris) { index, uri ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected Media",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Remove button
                            IconButton(
                                onClick = { viewModel.onEvent(CreatePostEvent.RemoveMedia(index)) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.7f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Media",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}