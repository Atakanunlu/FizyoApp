package com.example.fizyoapp.presentation.socialmedia.socialmediamain

import android.net.Uri
import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.socialmedia.socialmedianavbar.PhysiotherapistSocialMediaNavbar
import com.example.fizyoapp.presentation.socialmedia.socialmedianavbar.UserSocialMediaNavbar
import com.example.fizyoapp.presentation.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

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

    LaunchedEffect(key1 = Unit) {
        viewModel.initializeScreen()
    }

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
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            userFullName,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
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
                            contentDescription = "Geri",
                            tint = Color.White
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
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = primaryColor
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
                        tint = primaryColor.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz gönderi paylaşılmamış",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
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
    var showFullMediaDialog by remember { mutableStateOf(false) }
    var selectedMediaIndex by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
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
                        .background(primaryColor.copy(alpha = 0.2f))
                        .clickable {
                            AppScreens.PhysiotherapistSocialProfile.route
                        }
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
                            tint = primaryColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = post.userName,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = if (post.userRole == "PHYSIOTHERAPIST") "Fizyoterapist" else "Kullanıcı",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (post.userRole == "PHYSIOTHERAPIST")
                            primaryColor else
                            textColor.copy(alpha = 0.6f)
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
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = post.content,
                lineHeight = 24.sp,
                color = textColor,
                modifier = Modifier.clickable { onClickDetail() }
            )
            if (post.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MultiMediaGallery(
                    mediaUrls = post.mediaUrls,
                    mediaTypes = post.mediaTypes,
                    onMediaClick = { index ->
                        selectedMediaIndex = index
                        showFullMediaDialog = true
                    }
                )
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
                        tint = if (isLikedByCurrentUser) missedCallColor else
                            textColor.copy(alpha = 0.6f)
                    )
                }
                Text(
                    text = "${post.likeCount} beğeni",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(
                    onClick = { onClickDetail() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = "Yorum",
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "${post.commentCount} yorum",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    if (showFullMediaDialog && post.mediaUrls.isNotEmpty()) {
        FullScreenMediaDialog(
            mediaUrl = post.mediaUrls[selectedMediaIndex],
            mediaType = post.mediaTypes.getOrNull(selectedMediaIndex) ?: "image",
            mediaUrls = post.mediaUrls,
            mediaTypes = post.mediaTypes,
            initialIndex = selectedMediaIndex,
            onDismiss = { showFullMediaDialog = false }
        )
    }
}

@Composable
fun MultiMediaGallery(
    mediaUrls: List<String>,
    mediaTypes: List<String>,
    onMediaClick: (Int) -> Unit
) {
    when {
        mediaUrls.size == 1 -> {
            val mediaUrl = mediaUrls.first()
            val isVideo = mediaTypes.firstOrNull() == "video"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onMediaClick(0) }
            ) {
                if (isVideo) {
                    VideoThumbnail(
                        videoUrl = mediaUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = "Medya",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        mediaUrls.size in 2..3 -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onMediaClick(0) }
                ) {
                    val isVideo = mediaTypes.getOrNull(0) == "video"
                    if (isVideo) {
                        VideoThumbnail(
                            videoUrl = mediaUrls[0],
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = mediaUrls[0],
                            contentDescription = "Medya",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1 until minOf(4, mediaUrls.size)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onMediaClick(i) }
                        ) {
                            val isVideo = mediaTypes.getOrNull(i) == "video"
                            if (isVideo) {
                                VideoThumbnail(
                                    videoUrl = mediaUrls[i],
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                AsyncImage(
                                    model = mediaUrls[i],
                                    contentDescription = "Medya",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (i == 3 && mediaUrls.size > 4) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(overlayColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${mediaUrls.size - 3}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                }
                            }
                        }
                    }
                    if (mediaUrls.size == 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        else -> {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 0..1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onMediaClick(i) }
                        ) {
                            val isVideo = mediaTypes.getOrNull(i) == "video"
                            if (isVideo) {
                                VideoThumbnail(
                                    videoUrl = mediaUrls[i],
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                AsyncImage(
                                    model = mediaUrls[i],
                                    contentDescription = "Medya",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 2..4) {
                        if (i < mediaUrls.size) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onMediaClick(i) }
                            ) {
                                val isVideo = mediaTypes.getOrNull(i) == "video"
                                if (isVideo) {
                                    VideoThumbnail(
                                        videoUrl = mediaUrls[i],
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    AsyncImage(
                                        model = mediaUrls[i],
                                        contentDescription = "Medya",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                if (i == 4 && mediaUrls.size > 5) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(overlayColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+${mediaUrls.size - 5}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoThumbnail(
    videoUrl: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(modifier = modifier) {
        var playerPrepared by remember { mutableStateOf(false) }
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.useController = false
                    val player = ExoPlayer.Builder(ctx).build().apply {
                        setMediaItem(MediaItem.fromUri(videoUrl))
                        prepare()
                        playWhenReady = false
                        volume = 0f  // Ses kapalı
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_READY) {
                                    play()
                                    pause()
                                    playerPrepared = true
                                }
                            }
                        })
                    }
                    this.player = player
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.Center)
                .background(
                    color = overlayColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Oynat",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun FullScreenMediaDialog(
    mediaUrl: String,
    mediaType: String,
    mediaUrls: List<String> = listOf(mediaUrl),
    mediaTypes: List<String> = listOf(mediaType),
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentIndex by remember { mutableStateOf(initialIndex) }
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    DisposableEffect(key1 = currentIndex) {
        exoPlayer?.release()
        exoPlayer = null
        if (mediaTypes.getOrNull(currentIndex) == "video") {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(mediaUrls[currentIndex])))
                prepare()
                play()
            }
        }
        onDispose {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            val currentMediaUrl = mediaUrls.getOrNull(currentIndex) ?: mediaUrl
            val currentMediaType = mediaTypes.getOrNull(currentIndex) ?: mediaType

            if (currentMediaType == "video") {
                exoPlayer?.let { player ->
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                this.player = player
                                useController = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                AsyncImage(
                    model = currentMediaUrl,
                    contentDescription = "Medya",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            if (mediaUrls.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentIndex > 0) {
                        IconButton(
                            onClick = {
                                currentIndex--
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(overlayColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Önceki",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                    if (currentIndex < mediaUrls.size - 1) {
                        IconButton(
                            onClick = {
                                currentIndex++
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(overlayColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Sonraki",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }

            if (mediaUrls.size > 1) {
                Text(
                    text = "${currentIndex + 1}/${mediaUrls.size}",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .background(
                            color = overlayColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(
                        color = overlayColor,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = Color.White
                )
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
            primaryColor.copy(alpha = 0.1f)
        else
            primaryColor,
        label = "backgroundColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isFollowing)
            primaryColor
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