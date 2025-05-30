package com.example.fizyoapp.presentation.advertisement.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdvertisementScreen(
    navController: NavController,
    viewModel: CreateAdvertisementViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var description by remember { mutableStateOf("") }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(key1 = state.hasActiveAdvertisement) {
        if (state.hasActiveAdvertisement) {
        }
    }

    LaunchedEffect(key1 = state.navigateToPayment) {
        if (state.navigateToPayment && selectedImageUri != null) {
            viewModel.setImageUri(selectedImageUri!!)
            viewModel.setDescription(description)
            navController.navigate(AppScreens.AdvertisementPaymentScreen.route)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Reklam Oluştur",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reklam Görselinizi ve Açıklamanızı Ekleyin",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = surfaceColor
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(context)
                                        .data(data = selectedImageUri)
                                        .build()
                                ),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = primaryColor
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Reklam Görseli Seçin",
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Reklam Açıklaması") },
                    placeholder = { Text("Reklamınız için kısa bir açıklama yazın...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor,
                        unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                        unfocusedLabelColor = primaryColor.copy(alpha = 0.7f)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = primaryColor.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Reklam Bilgisi",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "- Reklamınız 24 saat boyunca aktif kalır",
                            color = textColor
                        )
                        Text(
                            text = "- Kullanıcılara ana ekranda gösterilir",
                            color = textColor
                        )
                        Text(
                            text = "- Reklam ücreti: 50₺",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.onContinueClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedImageUri != null && description.isNotBlank() && !state.isLoading,
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
                        Text("Devam Et - 50₺")
                    }
                }

                if (state.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.error,
                        color = errorColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}