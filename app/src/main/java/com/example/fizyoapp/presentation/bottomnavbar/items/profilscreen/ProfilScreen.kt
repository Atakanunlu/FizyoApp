package com.example.fizyoapp.presentation.bottomnavbar.items.profilscreen
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.FollowTheSigns
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.user.usermainscreen.UserEvent
import com.example.fizyoapp.presentation.user.usermainscreen.UserViewModel
import com.example.fizyoapp.ui.bottomnavbar.BottomNavbarComponent
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilScreen(
    navController: NavController,
    viewModel: UserViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsState().value
    var showLogoutDialog by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFF3B3E68)
    val accentColor = Color(0xFF6D72C3)
    val backgroundColor = Color(0xFFF8F9FC)
    val cardColor = Color.White
    val errorColor = Color(0xFFE57373)

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UserViewModel.UiEvent.NavigateToLogin -> {
                    navController.navigate(AppScreens.LoginScreen.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Çıkış Yap", fontWeight = FontWeight.Bold) },
            text = { Text("Hesabınızdan çıkış yapmak istediğinize emin misiniz?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(UserEvent.Logout)
                        showLogoutDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = errorColor
                    )
                ) {
                    Text("Çıkış Yap")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("İptal")
                }
            },
            containerColor = cardColor,
            titleContentColor = primaryColor,
            iconContentColor = primaryColor
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profil",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavbarComponent(navController) },
        containerColor = backgroundColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
            ) {
                ProfileHeader(
                    userName = "${state.userProfile?.firstName} ${state.userProfile?.lastName}",
                    profilePhotoUrl = state.userProfile?.profilePhotoUrl,
                    onEditProfileClick = {
                        navController.navigate(AppScreens.UserInformationScreen.route)
                    },
                    primaryColor = primaryColor,
                    accentColor = accentColor
                )

                ProfileMenuSection(
                    navController = navController,
                    onLogoutClick = { showLogoutDialog = true },
                    cardColor = cardColor,
                    primaryColor = primaryColor,
                    accentColor = accentColor
                )
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userName: String,
    profilePhotoUrl: String?,
    onEditProfileClick: () -> Unit,
    primaryColor: Color,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor,
                            accentColor
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (!profilePhotoUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profilePhotoUrl)
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
                        contentDescription = "Profil",
                        tint = accentColor,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userName.ifEmpty { "Kullanıcı" },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onEditProfileClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = primaryColor
                ),
                modifier = Modifier.padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Düzenle",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Profili Düzenle",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ProfileMenuSection(
    navController: NavController,
    onLogoutClick: () -> Unit,
    cardColor: Color,
    primaryColor: Color,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Hesap Ayarları",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ProfileMenuItem(
                    icon = Icons.Outlined.Person,
                    title = "Kullanıcı Bilgileri",
                    subtitle = "Kişisel bilgilerinizi görüntüleyin",
                    primaryColor = primaryColor,
                    accentColor = accentColor
                ) {
                    navController.navigate(AppScreens.UserInformationScreen.route)
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )

                ProfileMenuItem(
                    icon = Icons.Outlined.Settings,
                    title = "Ayarlar",
                    subtitle = "Uygulama ayarlarını yönetin",
                    primaryColor = primaryColor,
                    accentColor = accentColor
                ) {
                    navController.navigate(AppScreens.SettingsScreen.route)
                }
            }
        }

        Text(
            text = "Özellikler",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp, top = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                ProfileMenuItem(
                    icon = Icons.Outlined.AttachMoney,
                    title = "Finansal Bilgiler",
                    subtitle = "Ödeme bilgilerinizi yönetin",
                    primaryColor = primaryColor,
                    accentColor = accentColor
                ) {
                    // Finansal bilgiler ekranına yönlendirme
                }

                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )

                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Outlined.FollowTheSigns,
                    title = "Takip Edilenler",
                    subtitle = "Takip ettiğiniz fizyoterapistleri yönetin",
                    primaryColor = primaryColor,
                    accentColor = accentColor
                ) {
                    // Takip edilenler ekranına yönlendirme
                }
            }
        }

        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Çıkış Yap"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Hesaptan Çıkış Yap",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    primaryColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = "İlerle",
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
    }
}