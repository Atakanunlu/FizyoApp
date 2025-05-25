package com.example.fizyoapp.presentation.bottomnavbar.items.searchscreen

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.bottomnavbar.BottomNavbarComponent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchScreenViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val primaryColor = Color(59, 62, 104)
    val backgroundColor = Color(245, 245, 250)

    var isSearchBarFocused by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SearchScreenViewModel.SearchScreenUiEvent.NavigateToPhysiotherapistDetail -> {
                    val route = "${AppScreens.PhysiotherapistDetailScreen.route}/${event.physiotherapistId}"
                    navController.navigate(route)
                }
            }
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Fizyoterapist Ara",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,
                    titleContentColor = Color.White
                ),
                actions = {
                    if (state.filteredPhysiotherapists.isNotEmpty()) {
                        IconButton(onClick = {
                            scope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Yukarı Çık",
                                tint = Color.White
                            )
                        }
                    }
                }
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Fizyoterapist Bul",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "İhtiyacınıza uygun fizyoterapisti arayın ve iletişime geçin",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                SearchBar(
                    searchQuery = state.searchQuery,
                    onSearchQueryChanged = { viewModel.onEvent(SearchScreenEvent.SearchQueryChanged(it)) },
                    onFocusChanged = { isSearchBarFocused = it },
                    isFocused = isSearchBarFocused,
                    accentColor = primaryColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    state.isLoading -> {
                        LoadingContent()
                    }

                    state.error != null -> {
                        ErrorContent(
                            errorMessage = state.error ?: "Bir hata oluştu",
                            onRetryClick = {
                                viewModel.onEvent(SearchScreenEvent.RefreshPhysiotherapists)
                            }
                        )
                    }

                    state.filteredPhysiotherapists.isEmpty() -> {
                        EmptyContent(
                            onRefreshClick = {
                                viewModel.onEvent(SearchScreenEvent.RefreshPhysiotherapists)
                            }
                        )
                    }

                    else -> {
                        PhysiotherapistList(
                            physiotherapists = state.filteredPhysiotherapists,
                            scrollState = scrollState,
                            onCardClick = { physiotherapistId ->
                                viewModel.onEvent(
                                    SearchScreenEvent.NavigateToPhysiotherapistDetail(
                                        physiotherapistId
                                    )
                                )
                            },
                            onMessageClick = { physiotherapistId ->
                                val route = AppScreens.MessagesDetailScreen.createMessageDetailRoute(physiotherapistId)
                                navController.navigate(route)
                            }
                        )
                    }
                }
            }

            val showScrollToTop by remember {
                derivedStateOf { scrollState.firstVisibleItemIndex > 5 }
            }

            if (showScrollToTop) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            scrollState.animateScrollToItem(0)
                        }
                    },
                    containerColor = primaryColor,
                    contentColor = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 80.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Başa Dön"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    isFocused: Boolean,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isFocused) 8.dp else 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .onFocusChanged { focusState ->
                    onFocusChanged(focusState.isFocused)
                },
            placeholder = {
                Text(
                    "İsim, şehir veya uzmanlık ara...",
                    color = Color.Gray.copy(alpha = 0.7f)
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Ara",
                    tint = accentColor
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onSearchQueryChanged("")
                        }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Temizle",
                            tint = accentColor
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = Color.LightGray,
                cursorColor = accentColor,
                containerColor = Color.White
            )
        )
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(59, 62, 104),
                strokeWidth = 4.dp,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fizyoterapistler yükleniyor...",
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ErrorContent(
    errorMessage: String,
    onRetryClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.7f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Color.DarkGray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetryClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(59, 62, 104)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tekrar Dene")
            }
        }
    }
}

@Composable
fun EmptyContent(
    onRefreshClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = Color(59, 62, 104).copy(alpha = 0.7f),
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Fizyoterapist bulunamadı",
                color = Color.DarkGray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Farklı arama kriterleri deneyebilir veya sonuçları yenileyebilirsiniz",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRefreshClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(59, 62, 104)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Yenile")
            }
        }
    }
}

@Composable
fun PhysiotherapistList(
    physiotherapists: List<PhysiotherapistProfile>,
    scrollState: LazyListState,
    onCardClick: (String) -> Unit,
    onMessageClick: (String) -> Unit
) {
    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(230, 230, 250)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(59, 62, 104),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${physiotherapists.size} fizyoterapist bulundu. Detaylı bilgi için profilleri inceleyebilirsiniz.",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        items(
            items = physiotherapists,
            key = { it.userId }
        ) { physiotherapist ->
            PhysiotherapistItem(
                physiotherapist = physiotherapist,
                onCardClick = { onCardClick(physiotherapist.userId) },
                onMessageClick = { onMessageClick(physiotherapist.userId) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PhysiotherapistItem(
    physiotherapist: PhysiotherapistProfile,
    onCardClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = RoundedCornerShape(16.dp)
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
                    .background(Color(59, 62, 104, 20)),
                contentAlignment = Alignment.Center
            ) {
                if (physiotherapist.profilePhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = physiotherapist.profilePhotoUrl,
                        contentDescription = "Profil fotoğrafı",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profil",
                        tint = Color(59, 62, 104),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "FZT. ${physiotherapist.firstName} ${physiotherapist.lastName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(59, 62, 104)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${physiotherapist.city} / ${physiotherapist.district}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = Color(59, 62, 104),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Fizyoterapist",
                        fontSize = 14.sp,
                        color = Color(59, 62, 104),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                OutlinedButton(
                    onClick = { onMessageClick() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(59, 62, 104)
                    ),
                    border = BorderStroke(1.dp, Color(59, 62, 104))
                ) {
                    Icon(
                        imageVector = Icons.Default.Message,
                        contentDescription = "Mesaj",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mesaj",
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onCardClick() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(59, 62, 104)
                    )
                ) {
                    Text(
                        text = "Detay",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Detay",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}