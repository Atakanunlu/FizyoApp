package com.example.fizyoapp.presentation.socialmedia.socialmediasearch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.fizyoapp.presentation.socialmedia.socialmedianavbar.PhysiotherapistSocialMediaNavbar
import com.example.fizyoapp.presentation.socialmedia.socialmedianavbar.UserSocialMediaNavbar
import com.example.fizyoapp.presentation.ui.theme.*

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

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Fokus hatası görmezden gelinir
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Fizyoterapist Ara",
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
                    if (state.searchHistory.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearSearchHistory() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Arama Geçmişini Temizle",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
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
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
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
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = primaryColor
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onSearchQueryChange("") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Temizle",
                                tint = primaryColor
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    unfocusedBorderColor = primaryColor.copy(alpha = 0.5f),
                    focusedContainerColor = surfaceColor,
                    unfocusedContainerColor = surfaceColor,
                    cursorColor = primaryColor
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = primaryColor
                    )
                } else {
                    LazyColumn {
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
                                        tint = primaryColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Arama Sonuçları",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = "${state.searchResults.size} sonuç",
                                        fontSize = 14.sp,
                                        color = textColor.copy(alpha = 0.6f)
                                    )
                                }
                            }
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
                                            tint = primaryColor.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Sonuç bulunamadı",
                                            fontSize = 18.sp,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
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
                                        tint = primaryColor,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Geçmiş Aramalarım",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }
                            }
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
                        item {
                            Spacer(modifier = Modifier.height(50.dp))
                        }
                    }
                }
                if (state.error != null) {
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        containerColor = errorColor,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(state.error!!)
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.1f))
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
                        tint = primaryColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${physiotherapist.firstName} ${physiotherapist.lastName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "Fizyoterapist",
                    fontSize = 14.sp,
                    color = primaryColor
                )
                if (physiotherapist.city.isNotEmpty()) {
                    Text(
                        text = physiotherapist.city,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                imageVector = if (showHistoryIcon)
                    Icons.Default.History
                else
                    Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = if (showHistoryIcon)
                    Color.Gray
                else
                    primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}