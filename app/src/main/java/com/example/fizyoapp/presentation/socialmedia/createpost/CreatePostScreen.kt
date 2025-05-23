package com.example.fizyoapp.presentation.socialmedia.createpost

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val textColor = Color.DarkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    navController: NavController,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showMediaOptions by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        viewModel.onEvent(CreatePostEvent.MediaAdded(uris.map { it.toString() }))
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        viewModel.onEvent(CreatePostEvent.MediaAdded(uris.map { it.toString() }))
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            photoPickerLauncher.launch("image/*")
        }
    }

    val videoPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            videoPickerLauncher.launch("video/*")
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is CreatePostViewModel.UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gönderi Oluştur",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    val isEnabled = !state.isLoading && (state.content.isNotBlank() || state.mediaUris.isNotEmpty())
                    TextButton(
                        onClick = { viewModel.onEvent(CreatePostEvent.CreatePost) },
                        enabled = isEnabled
                    ) {
                        Text(
                            text = "Paylaş",
                            color = Color.White.copy(alpha = if (isEnabled) 1f else 0.5f)
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
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.2f))
                            ) {
                                if (state.currentUserPhotoUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = state.currentUserPhotoUrl,
                                        contentDescription = "Profil fotoğrafı",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.Center),
                                        tint = primaryColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = state.currentUserName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = textColor
                                )
                                Text(
                                    text = "Fizyoterapist",
                                    fontSize = 14.sp,
                                    color = primaryColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.content,
                            onValueChange = { viewModel.onEvent(CreatePostEvent.ContentChanged(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 150.dp),
                            placeholder = { Text("Düşüncelerinizi paylaşın...") },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                containerColor = surfaceColor,
                                cursorColor = primaryColor
                            ),
                            maxLines = 10
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.mediaUris.isNotEmpty()) {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.mediaUris) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    ) {
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = "Seçilen medya",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        IconButton(
                                            onClick = { viewModel.onEvent(CreatePostEvent.MediaRemoved(uri)) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.6f))
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
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

                        Button(
                            onClick = { showMediaOptions = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor.copy(alpha = 0.1f),
                                contentColor = primaryColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Fotoğraf/Video Ekle"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fotoğraf/Video Ekle")
                        }
                    }
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFB71C1C)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.error!!,
                                color = Color(0xFFB71C1C)
                            )
                        }
                    }
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
        }
    }

    if (showMediaOptions) {
        AlertDialog(
            onDismissRequest = { showMediaOptions = false },
            title = { Text("Medya Ekle") },
            text = {
                Column {
                    DropdownMenuItem(
                        text = { Text("Fotoğraf Ekle (Birden Fazla)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Fotoğraf",
                                tint = primaryColor
                            )
                        },
                        onClick = {
                            val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    permission
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                photoPickerLauncher.launch("image/*")
                            } else {
                                storagePermissionLauncher.launch(permission)
                            }
                            showMediaOptions = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Video Ekle") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video",
                                tint = primaryColor
                            )
                        },
                        onClick = {
                            val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_VIDEO
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    permission
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                videoPickerLauncher.launch("video/*")
                            } else {
                                videoPermissionLauncher.launch(permission)
                            }
                            showMediaOptions = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showMediaOptions = false }) {
                    Text("İptal", color = primaryColor)
                }
            },
            containerColor = surfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }
}