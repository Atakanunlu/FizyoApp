package com.example.fizyoapp.presentation.bottomnavbar.items.profilscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.domain.model.auth.User
import com.example.fizyoapp.presentation.user.usermainscreen.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen( navController: NavController,
                    viewModel: UserViewModel = hiltViewModel()
) {

    val state = viewModel.state.collectAsState().value
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            ProfileSection(
                name = "${state.userProfile?.firstName} ${state.userProfile?.lastName}",
                email = state.email,
                onClick = { navController.navigate("profile_settings") }
            )

            Divider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            SettingsSectionTitle(title = "Kişisel Ayarlar")

            SettingsItem(
                icon = Icons.Filled.Person,
                title = "Profil ve Hesap Ayarları",
                subtitle = "Kişisel bilgiler, doğum tarihi",
                onClick = { navController.navigate("profile_settings") }
            )

            SettingsItem(
                icon = Icons.Filled.Notifications,
                title = "Bildirim Ayarları",
                subtitle = "İlaç hatırlatıcıları, randevu hatırlatmaları",
                onClick = { navController.navigate("notification_settings") }
            )

            SettingsItem(
                icon = Icons.Filled.Lock,
                title = "Gizlilik ve Güvenlik",
                subtitle = "Veri paylaşımı, doktor erişimi, uygulama kilidi",
                onClick = { navController.navigate("privacy_settings") }
            )

            Divider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            SettingsSectionTitle(title = "Uygulama Ayarları")


            SettingsItem(
                icon = Icons.Filled.Palette,
                title = "Görünüm ve Kişiselleştirme",
                subtitle = "Karanlık-Aydınlık Tema",
                onClick = {},
                endContent = {
                    ThemeSwitch()
                }
            )

            SettingsItem(
                icon = Icons.Filled.Language,
                title = "Dil ve Bölge Ayarları",
                subtitle = "Uygulama dili, tarih ve saat formatı",
                onClick = { navController.navigate("language_settings") }
            )


            Divider(Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            SettingsSectionTitle(title = "Destek ve Bilgi")

            SettingsItem(
                icon = Icons.Filled.Help,
                title = "Yardım ve Destek",
                subtitle = "SSS, video eğitimler, müşteri desteği",
                onClick = { navController.navigate("support") }
            )

            SettingsItem(
                icon = Icons.Filled.Info,
                title = "Uygulama Hakkında",
                subtitle = "Sürüm 1.0.0, lisans bilgileri",
                onClick = { navController.navigate("about") }
            )

            Button(
                onClick = { /* Çıkış işlemleri */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Çıkış Yap")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileSection(
    name: String,
    email: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                Icons.Default.AccountCircle,
                contentDescription = "Profil",
                modifier = Modifier
                    .size(60.dp)
                    .padding(4.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = email,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            IconButton(onClick = onClick) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Profili Düzenle",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    endContent: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 4.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))


            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            endContent?.invoke() ?: Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ThemeSwitch() {
    var darkMode by remember { mutableStateOf(false) }

    Switch(
        checked = darkMode,
        onCheckedChange = { darkMode = it }
    )
}