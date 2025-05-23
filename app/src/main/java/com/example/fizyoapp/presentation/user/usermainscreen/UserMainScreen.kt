package com.example.fizyoapp.presentation.user.usermainscreen

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import com.example.fizyoapp.presentation.advertisement.banner.AdvertisementBannerState
import com.example.fizyoapp.presentation.advertisement.banner.AdvertisementBannerViewModel
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.bottomnavbar.BottomNavbarComponent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserMainScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel(),
    advertisementBannerViewModel: AdvertisementBannerViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val adState = advertisementBannerViewModel.state.collectAsState().value
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Reklamları uygulama açıldığında hemen yükle
    LaunchedEffect(key1 = Unit) {
        Log.d("UserMainScreen", "LaunchedEffect: Reklamlar yükleniyor")
        advertisementBannerViewModel.loadActiveAdvertisements()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d("UserMainScreen", "ON_RESUME: Veriler ve reklamlar yenileniyor")
                val userId = state.userProfile?.userId
                if (userId != null) {
                    viewModel.refreshAllData(userId)
                    advertisementBannerViewModel.loadActiveAdvertisements()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UserViewModel.UiEvent.NavigateToLogin -> {
                    navController.navigate("login_screen") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val displayName = when {
                            state.userProfile?.firstName?.isNotEmpty() == true ->
                                "${state.userProfile.firstName} ${state.userProfile.lastName}".trim()
                            state.userName != null -> state.userName
                            else -> "Hasta"
                        }
                        Text("Merhaba, $displayName")
                        Text(
                            "İyi günler",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Ayarlar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(59, 62, 104),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavbarComponent(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(59, 62, 104))
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center),
                    color = Color.White
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Reklam banner'ı en üstte gösteriyoruz
                    item {
                        AdvertisementBanner(
                            navController = navController,
                            adState = adState,
                            viewModel = advertisementBannerViewModel
                        )
                    }

                    item {
                        MainNavigationButtons(navController)
                        PainSymptomSummaryCard(navController, state.latestPainRecord)
                    }

                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }

            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.onEvent(UserEvent.DismissError) }) {
                            Text("Tamam", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFFB71C1C)
                ) {
                    Text(state.error, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AdvertisementBanner(
    navController: NavController,
    adState: AdvertisementBannerState,
    viewModel: AdvertisementBannerViewModel
) {
    val context = LocalContext.current

    // Reklamlar varsa
    if (adState.advertisements.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Mevcut reklam
                adState.currentAdvertisement?.let { ad ->
                    // Resmi göster
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                Log.d("AdvertisementBanner", "Reklama tıklandı: ${ad.id}")
                                navController.navigate(AppScreens.AdvertisementDetailScreen.createRoute(ad.id))
                            }
                    ) {
                        AsyncImage(
                            model = ad.imageUrl,
                            contentDescription = "Reklam",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onSuccess = {
                                Log.d("AdvertisementBanner", "Reklam görseli başarıyla yüklendi")
                            },
                            onError = {
                                Log.e("AdvertisementBanner", "Reklam görseli yüklenemedi: ${ad.imageUrl}")
                            }
                        )

                        // Reklam badge'i
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Reklam",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Sol-sağ kaydırma okları
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Sol ok
                    IconButton(
                        onClick = { viewModel.moveToPreviousAd() },
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(36.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Önceki",
                            tint = Color.White
                        )
                    }

                    // Sağ ok
                    IconButton(
                        onClick = { viewModel.moveToNextAd() },
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(36.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Sonraki",
                            tint = Color.White
                        )
                    }
                }

                // Alt kısımda nokta göstergeleri
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    adState.advertisements.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == adState.currentIndex)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.White.copy(alpha = 0.6f)
                                )
                                .clickable { viewModel.moveToAd(index) }
                        )
                    }
                }
            }
        }
    } else if (adState.isLoading) {
        // Yükleniyor durumu
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    } else {
        // Reklam yoksa
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Reklam Alanı",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun MainNavigationButtons(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .height(130.dp),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 6.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.DarkGray
            )
        ) {
            Text(
                text = "EGZERSİZLERİM",
                fontStyle = FontStyle.Italic,
                style = TextStyle(fontSize = 20.sp)
            )
            Icon(
                imageVector = Icons.Filled.AccessibilityNew,
                contentDescription = null,
                Modifier
                    .padding(start = 17.dp)
                    .size(40.dp)
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = { navController.navigate(AppScreens.OrnekEgzersizler.route) },
            modifier = Modifier
                .height(130.dp)
                .weight(1f),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 6.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.DarkGray
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessibilityNew,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Örnek Egzersizler",
                    style = TextStyle(fontSize = 17.sp),
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }
        }
        Button(
            onClick = { navController.navigate(AppScreens.HastaliklarimScreen.route) },
            colors = ButtonDefaults.buttonColors(
                contentColor = Color.DarkGray,
                containerColor = Color.White
            ),
            modifier = Modifier
                .height(130.dp)
                .weight(1f),
            shape = RoundedCornerShape(10.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 6.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Healing,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hastalıklarım",
                    fontStyle = FontStyle.Italic,
                    style = TextStyle(fontSize = 17.sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = { navController.navigate(AppScreens.RehabilitationHistoryScreen.route) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.DarkGray
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .height(130.dp),
        ) {
            Text(
                text = "Rehabilitasyon Geçmişim  ",
                fontStyle = FontStyle.Italic,
                style = TextStyle(fontSize = 20.sp)
            )
            Icon(
                imageVector = Icons.Filled.History,
                contentDescription = null,
                modifier = Modifier.size(30.dp)
            )
        }
    }
    Button(
        onClick = { navController.navigate(AppScreens.SocialMediaScreen.route) },
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.DarkGray
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 10.dp,
            pressedElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp)
            .height(150.dp),
    ) {
        Text(
            text = "Sosyal Medya  ",
            fontStyle = FontStyle.Italic,
            style = TextStyle(fontSize = 20.sp)
        )
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun PainSymptomSummaryCard(navController: NavController, painRecord: PainRecord?) {
    val intensity = painRecord?.intensity ?: 0
    val location = painRecord?.location ?: "Veri yok"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { navController.navigate("pain_tracking") },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ağrı Takibi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Text(
                    text = if (painRecord != null) {
                        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        dateFormat.format(Date(painRecord.timestamp))
                    } else {
                        "Bugün"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Şiddet:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier.weight(1f)
                ) {
                    repeat(10) { index ->
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(horizontal = 2.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < intensity) {
                                        when (index) {
                                            0, 1, 2 -> Color(0xFF388E3C)
                                            3, 4, 5 -> Color(0xFFFFA000)
                                            else -> Color(0xFFE53935)
                                        }
                                    } else {
                                        Color.LightGray
                                    }
                                )
                        )
                    }
                }
                Text(
                    text = "$intensity/10",
                    fontWeight = FontWeight.Bold,
                    color = Color(59, 62, 104),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Lokasyon: $location",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}