// presentation/socialmedia/SocialMediaSearchScreen.kt
package com.example.fizyoapp.presentation.socialmedia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.presentation.navigation.AppScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaSearchScreen(
    navController: NavController,
    viewModel: SocialMediaSearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
    val isPhysiotherapist = currentUser?.role == UserRole.PHYSIOTHERAPIST
    val focusRequester = remember { FocusRequester() }

    // Ekran açıldığında arama çubuğuna otomatik focus
    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Hata durumunda devam et
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Fizyoterapist Ara",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    if (state.searchHistory.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearSearchHistory() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Arama Geçmişini Temizle"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isPhysiotherapist) {
                PhysiotherapistSocialMediaNavbar(navController, currentRoute)
            } else {
                UserSocialMediaNavbar(navController, currentRoute)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Arama Çubuğu
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .focusRequester(focusRequester),
                placeholder = {
                    Text(
                        "Fizyoterapist adı veya soyadı ara...",
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChange("") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Temizle"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )

            // İçerik Alanı
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn {
                        // Arama yapıldığında ve sonuçlar varsa
                        if (state.hasSearched && state.searchResults.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonSearch,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Arama Sonuçları",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "${state.searchResults.size} sonuç",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Arama sonuçları listesi
                            items(state.searchResults) { physiotherapist ->
                                PhysiotherapistItem(
                                    physiotherapist = physiotherapist,
                                    onClick = {
                                        viewModel.addToSearchHistory(physiotherapist)
                                        navController.navigate(
                                            "${AppScreens.PhysiotherapistSocialProfile.route}/${physiotherapist.userId}"
                                        )
                                    }
                                )
                            }
                        }
                        // Arama yapıldı ama sonuç yoksa
                        else if (state.hasSearched && state.searchResults.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SearchOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(80.dp),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Sonuç bulunamadı",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }
                        }

                        // Arama Geçmişi (arama yapılmadığında göster)
                        if (state.searchHistory.isNotEmpty() && !state.hasSearched) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Geçmiş Aramalarım",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Geçmiş aramaları listele
                            items(state.searchHistory) { physiotherapist ->
                                PhysiotherapistItem(
                                    physiotherapist = physiotherapist,
                                    onClick = {
                                        viewModel.addToSearchHistory(physiotherapist)
                                        navController.navigate(
                                            "${AppScreens.PhysiotherapistSocialProfile.route}/${physiotherapist.userId}"
                                        )
                                    },
                                    showHistoryIcon = true
                                )
                            }
                        }

                        // Alt boşluk
                        item {
                            Spacer(modifier = Modifier.height(50.dp))
                        }
                    }
                }

                // Hata gösterimi
                if (state.error != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Snackbar {
                            Text(state.error!!)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhysiotherapistItem(
    physiotherapist: PhysiotherapistProfile,
    onClick: () -> Unit,
    showHistoryIcon: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profil Fotoğrafı
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                if (physiotherapist.profilePhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = physiotherapist.profilePhotoUrl,
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // İsim ve Soyisim
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${physiotherapist.firstName} ${physiotherapist.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Fizyoterapist",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (physiotherapist.city.isNotEmpty()) {
                    Text(
                        text = physiotherapist.city,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // İkon
            Icon(
                imageVector = if (showHistoryIcon)
                    Icons.Default.History
                else
                    Icons.Default.ArrowForward,
                contentDescription = null,
                tint = if (showHistoryIcon)
                    Color.Gray
                else
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}