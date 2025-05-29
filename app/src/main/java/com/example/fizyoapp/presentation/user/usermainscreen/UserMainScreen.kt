package com.example.fizyoapp.presentation.user.usermainscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fizyoapp.domain.model.usermainscreen.PainRecord
import com.example.fizyoapp.presentation.advertisement.banner.AdvertisementBannerState
import com.example.fizyoapp.presentation.advertisement.banner.AdvertisementBannerViewModel
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.bottomnavbar.BottomNavbarComponent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val accentColor = Color(59, 62, 104)
private val textColor = Color.DarkGray
private val socialMediaColor = Color(76, 175, 80)

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
    val showLogoutDialog = remember { mutableStateOf(false) }
    val retryCount = remember { mutableStateOf(0) }

    LaunchedEffect(key1 = Unit) {
        advertisementBannerViewModel.loadActiveAdvertisements()
        if (state.userProfile == null && !state.isLoading) {
            viewModel.onEvent(UserEvent.LoadUserProfile)
        }
    }

    LaunchedEffect(key1 = Unit) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && (state.userProfile == null || state.userProfile.userId != currentUser.uid)) {
                viewModel.onEvent(UserEvent.LoadUserProfile)
            }
        } catch (e: Exception) {
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                try {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null) {
                        if (state.userProfile == null) {
                            viewModel.onEvent(UserEvent.LoadUserProfile)
                        } else {
                            viewModel.refreshAllData(currentUser.uid)
                            advertisementBannerViewModel.loadActiveAdvertisements()
                        }
                    } else if (!state.isLoading) {
                        navController.navigate("login_screen") {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    val userId = state.userProfile?.userId
                    if (userId != null) {
                        viewModel.refreshAllData(userId)
                        advertisementBannerViewModel.loadActiveAdvertisements()
                    } else if (!state.isLoading) {
                        viewModel.onEvent(UserEvent.LoadUserProfile)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(60000)
            advertisementBannerViewModel.loadActiveAdvertisements()
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

    if (state.isLoading && retryCount.value < 3) {
        LaunchedEffect(key1 = state.isLoading) {
            delay(10000)
            if (state.isLoading) {
                retryCount.value++
                viewModel.onEvent(UserEvent.LoadUserProfile)
            }
        }
    }

    if (showLogoutDialog.value) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog.value = false },
            title = { Text("Çıkış Yap") },
            text = { Text("Çıkış yapmak istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog.value = false
                        try {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login_screen") {
                                popUpTo(navController.graph.id) { inclusive = true }
                            }
                        } catch (e: Exception) {
                            viewModel.onEvent(UserEvent.SignOut)
                        }
                    }
                ) {
                    Text("Evet", color = primaryColor)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog.value = false }
                ) {
                    Text("Hayır", color = primaryColor)
                }
            },
            containerColor = surfaceColor,
            titleContentColor = primaryColor,
            textContentColor = textColor
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Merhaba,",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        val displayName = when {
                            state.userProfile?.firstName?.isNotEmpty() == true ->
                                state.userProfile.firstName.trim()
                            else -> "Hasta"
                        }
                        Text(
                            displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(AppScreens.UserInformationScreen.route) }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profil"
                        )
                    }
                    IconButton(onClick = { showLogoutDialog.value = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Çıkış"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
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
                .background(backgroundColor)
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            color = primaryColor,
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Veriler yükleniyor...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = primaryColor
                        )
                        var showEmergencyButton by remember { mutableStateOf(false) }
                        LaunchedEffect(key1 = true) {
                            delay(3000)
                            showEmergencyButton = true
                        }
                        if (showEmergencyButton) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    viewModel.onEvent(UserEvent.LoadUserProfile)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                )
                            ) {
                                Text("Yeniden Dene")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    try {
                                        FirebaseAuth.getInstance().signOut()
                                    } catch (e: Exception) { }
                                    navController.navigate("login_screen") {
                                        popUpTo(navController.graph.id) { inclusive = true }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                )
                            ) {
                                Text("Acil Çıkış", color = Color.White)
                            }
                        }
                    }
                }
            } else if (state.userProfile == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Kullanıcı verileri yüklenemedi",
                            style = MaterialTheme.typography.bodyLarge,
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.onEvent(UserEvent.LoadUserProfile) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor
                            )
                        ) {
                            Text("Yeniden Dene")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                try {
                                    FirebaseAuth.getInstance().signOut()
                                } catch (e: Exception) { }
                                navController.navigate("login_screen") {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        ) {
                            Text("Çıkış Yap", color = Color.Red)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        AdvertisementBanner(
                            navController = navController,
                            adState = adState,
                            viewModel = advertisementBannerViewModel
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        WelcomeCard(state)
                        Spacer(modifier = Modifier.height(16.dp))
                        MainNavigationButtonsRedesigned(navController, state.latestPainRecord)
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
                        Row {
                            TextButton(onClick = { viewModel.onEvent(UserEvent.LoadUserProfile) }) {
                                Text("Yeniden Dene", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(onClick = { viewModel.onEvent(UserEvent.DismissError) }) {
                                Text("Kapat", color = Color.White)
                            }
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
fun WelcomeCard(state: UserState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Hoş Geldiniz",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fizyoterapi uygulamanıza hoş geldiniz. Egzersizlerinizi takip edebilir, ağrılarınızı kaydedebilir ve rehabilitasyon sürecinizi yönetebilirsiniz.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!state.userProfile?.profilePhotoUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(state.userProfile?.profilePhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profil Fotoğrafı",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = android.R.drawable.ic_menu_myplaces)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = when {
                        state.userProfile?.firstName?.isNotEmpty() == true && state.userProfile.lastName?.isNotEmpty() == true ->
                            "${state.userProfile.firstName} ${state.userProfile.lastName}".trim()
                        state.userProfile?.firstName?.isNotEmpty() == true ->
                            state.userProfile.firstName.trim()
                        state.userProfile?.lastName?.isNotEmpty() == true ->
                            state.userProfile.lastName.trim()
                        else -> "Hasta"
                    },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
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
    if (adState.advertisements.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                adState.currentAdvertisement?.let { ad ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                navController.navigate(AppScreens.AdvertisementDetailScreen.createRoute(ad.id))
                            }
                    ) {
                        AsyncImage(
                            model = ad.imageUrl,
                            contentDescription = "Reklam",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    color = primaryColor.copy(alpha = 0.7f),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Önceki",
                            tint = Color.White
                        )
                    }
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
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Sonraki",
                            tint = Color.White
                        )
                    }
                }
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
                                        primaryColor
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
            }
        }
    }
}

@Composable
fun MainNavigationButtonsRedesigned(navController: NavController, painRecord: PainRecord?) {
    Text(
        text = "Hizmetler",
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = primaryColor
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(AppScreens.UserExercisePlansScreen.route) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessibilityNew,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "EGZERSİZLERİM",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = "Kişisel egzersiz planlarınıza göz atın ve takip edin",
                    fontSize = 14.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = primaryColor
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ServiceCard(
            title = "Örnek Egzersizler",
            icon = Icons.Default.FitnessCenter,
            onClick = { navController.navigate(AppScreens.OrnekEgzersizler.route) },
            modifier = Modifier.weight(1f)
        )
        ServiceCard(
            title = "Hastalıklarım",
            icon = Icons.Default.Healing,
            onClick = { navController.navigate(AppScreens.HastaliklarimScreen.route) },
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    ServiceCard(
        title = "Rehabilitasyon Geçmişim",
        icon = Icons.Default.History,
        description = "Geçmiş tedavi ve rehabilitasyon süreçlerinizi görüntüleyin",
        onClick = {
            navController.navigate(AppScreens.RehabilitationHistoryScreen.route) {
                popUpTo(AppScreens.RehabilitationHistoryScreen.route) {
                    inclusive = true
                }
            }
        }
    )

    Spacer(modifier = Modifier.height(12.dp))

    ServiceCard(
        title = "Sosyal Medya",
        icon = Icons.Default.Share,
        description = "Paylaşımları görüntüleyin ve fizyoterapistlerle etkileşime geçin",
        onClick = { navController.navigate(AppScreens.SocialMediaScreen.route) },
        accentColor = socialMediaColor
    )

    Spacer(modifier = Modifier.height(12.dp))

    PainTrackingCard(painRecord, navController)
}

@Composable
fun ServiceCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    accentColor: Color = primaryColor
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                textAlign = TextAlign.Center
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PainTrackingCard(painRecord: PainRecord?, navController: NavController) {
    val intensity = painRecord?.intensity ?: 0
    val location = painRecord?.location ?: "Veri yok"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("pain_tracking") },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Ağrı Takibi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
                Text(
                    text = if (painRecord != null) {
                        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                        dateFormat.format(Date(painRecord.timestamp))
                    } else {
                        "Bugün"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Şiddet:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontWeight = FontWeight.Medium
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
                    color = primaryColor,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lokasyon:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate("pain_tracking") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor.copy(alpha = 0.1f),
                    contentColor = primaryColor
                ),
                modifier = Modifier.align(Alignment.End),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Detayları Görüntüle")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}