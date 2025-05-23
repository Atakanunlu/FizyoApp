package com.example.fizyoapp.presentation.socialmedia.socialmediamain

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.outlined.Feed
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.socialmedia.socialmedianavbar.PhysiotherapistSocialMediaNavbar
import com.example.fizyoapp.presentation.socialmedia.socialmedianavbar.UserSocialMediaNavbar
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaScreen(
    navController: NavController,
    viewModel: SocialMediaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
    val isPhysiotherapist = currentUser?.role == UserRole.PHYSIOTHERAPIST
    val userFullName = if (isPhysiotherapist) {
        viewModel.physiotherapistProfile.collectAsState().value?.let {
            "${it.firstName} ${it.lastName}"
        } ?: "Fizyoterapist"
    } else {
        viewModel.userProfile.collectAsState().value?.let {
            "${it.firstName} ${it.lastName}"
        } ?: "Kullanıcı"
    }

    // Ekran ilk açıldığında verileri yükle
    LaunchedEffect(key1 = Unit) {
        viewModel.initializeScreen()
    }

    // Sayfa her görünür olduğunda takip durumlarını güncelle
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d("SocialMediaScreen", "Screen resumed, refreshing follow states")
                viewModel.checkAllFollowStates()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Merhaba,",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            userFullName,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isPhysiotherapist) {
                            navController.navigate(AppScreens.PhysiotherapistMainScreen.route) {
                                popUpTo(AppScreens.SocialMediaScreen.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(AppScreens.UserMainScreen.route) {
                                popUpTo(AppScreens.SocialMediaScreen.route) { inclusive = true }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    if (isPhysiotherapist) {
                        FilledTonalIconButton(
                            onClick = { navController.navigate(AppScreens.CreatePostScreen.route) },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Paylaşım Yap"
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (state.posts.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Feed,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz gönderi paylaşılmamış",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.posts) { post ->
                        val isFollowingAuthor = viewModel.followStateMap.collectAsState().value[post.userId] ?: false
                        val isFollowLoading = viewModel.followLoadingMap.collectAsState().value[post.userId] ?: false

                        PostItem(
                            post = post,
                            currentUserId = currentUser?.id,
                            isFollowingAuthor = isFollowingAuthor,
                            isFollowLoading = isFollowLoading,
                            onClickDetail = {
                                navController.navigate(AppScreens.PostDetailScreen.createRoute(post.id))
                            },
                            onLike = {
                                viewModel.onLikePost(post.id)
                            },
                            onFollow = {
                                viewModel.toggleFollow(post.userId)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Text(state.error!!)
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    currentUserId: String?,
    isFollowingAuthor: Boolean,
    isFollowLoading: Boolean,
    onClickDetail: () -> Unit,
    onLike: () -> Unit,
    onFollow: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val isLikedByCurrentUser = post.likedBy.contains(currentUserId)
    val isCurrentUserPost = post.userId == currentUserId
    val canFollow = post.userRole == "PHYSIOTHERAPIST" && !isCurrentUserPost

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickDetail() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    if (post.userPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = post.userPhotoUrl,
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
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = post.userName,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (post.userRole == "PHYSIOTHERAPIST") "Fizyoterapist" else "Kullanıcı",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (post.userRole == "PHYSIOTHERAPIST")
                            MaterialTheme.colorScheme.primary else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                if (canFollow) {
                    FollowButton(
                        isFollowing = isFollowingAuthor,
                        isLoading = isFollowLoading,
                        onClick = onFollow
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateFormat.format(post.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            if (post.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = post.mediaUrls.first(),
                        contentDescription = "Gönderi görüntüsü",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (post.mediaUrls.size > 1) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+${post.mediaUrls.size - 1}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onLike() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isLikedByCurrentUser)
                            Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (isLikedByCurrentUser) Color.Red else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "${post.likeCount} beğeni",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Comment,
                    contentDescription = "Yorum",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.commentCount} yorum",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.weight(1f))
                FilledTonalButton(
                    onClick = { onClickDetail() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Detaylar")
                }
            }
        }
    }
}

@Composable
fun FollowButton(
    isFollowing: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isFollowing)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.primary,
        label = "backgroundColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isFollowing)
            MaterialTheme.colorScheme.primary
        else
            Color.White,
        label = "contentColor"
    )

    Button(
        onClick = onClick,
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.6f),
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        } else {
            if (isFollowing) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Takip Ediliyor",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Takip Ediliyor",
                    fontSize = 10.sp
                )
            } else {
                Text(
                    text = "Takip Et",
                    fontSize = 10.sp
                )
            }
        }
    }
}