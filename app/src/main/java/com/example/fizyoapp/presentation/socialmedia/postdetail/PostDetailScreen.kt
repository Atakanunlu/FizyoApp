package com.example.fizyoapp.presentation.socialmedia.postdetail

import android.content.Context
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.socialmedia.Comment
import java.text.SimpleDateFormat
import java.util.*

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val textColor = Color.DarkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val commentText by viewModel.commentText.collectAsState()
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    var videoControlsVisible by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(key1 = state.postDeleted) {
        if (state.postDeleted) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gönderi Detayı",
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
                    if (state.post?.userId == state.currentUserId) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Gönderiyi Sil",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                state.post?.id?.let { postId ->
                                    navController.navigate("edit_post_screen/$postId")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Gönderiyi Düzenle",
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
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFB71C1C),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error ?: "",
                        color = Color(0xFFB71C1C)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadPostAndComments() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tekrar Dene")
                    }
                }
            } else if (state.post == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Feed,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = primaryColor.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Gönderi bulunamadı",
                        color = textColor
                    )
                }
            } else {
                val post = state.post
                if (post != null) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(primaryColor.copy(alpha = 0.2f))
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
                                            Column {
                                                Text(
                                                    text = post.userName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = textColor
                                                )
                                                Text(
                                                    text = if (post.userRole == "PHYSIOTHERAPIST") "Fizyoterapist" else "Kullanıcı",
                                                    fontSize = 14.sp,
                                                    color = primaryColor
                                                )
                                            }
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = dateFormatter.format(post.timestamp),
                                                fontSize = 12.sp,
                                                color = textColor.copy(alpha = 0.6f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = post.content,
                                            modifier = Modifier.fillMaxWidth(),
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            color = textColor
                                        )
                                        if (post.mediaUrls.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(16.dp))
                                            MediaGallery(
                                                mediaUrls = post.mediaUrls,
                                                mediaTypes = post.mediaTypes,
                                                onVideoClick = { videoControlsVisible = !videoControlsVisible },
                                                videoControlsVisible = videoControlsVisible,
                                                context = context
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.onLikePost() },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (state.isPostLikedByCurrentUser)
                                                        Icons.Default.Favorite else
                                                        Icons.Default.FavoriteBorder,
                                                    contentDescription = "Beğen",
                                                    tint = if (state.isPostLikedByCurrentUser)
                                                        Color.Red else
                                                        textColor.copy(alpha = 0.6f)
                                                )
                                            }
                                            Text(
                                                text = "${post.likeCount} beğeni",
                                                fontSize = 14.sp,
                                                color = textColor.copy(alpha = 0.6f)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Comment,
                                                contentDescription = "Yorum",
                                                tint = textColor.copy(alpha = 0.6f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${post.commentCount} yorum",
                                                fontSize = 14.sp,
                                                color = textColor.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "Yorumlar",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = primaryColor
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = primaryColor.copy(alpha = 0.2f)
                                )
                            }

                            if (state.comments.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                                contentDescription = null,
                                                tint = textColor.copy(alpha = 0.4f),
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Henüz yorum yapılmamış",
                                                color = textColor.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                }
                            } else {
                                items(state.comments) { comment ->
                                    CommentItem(comment = comment, dateFormatter = dateFormatter)
                                }
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .navigationBarsPadding(),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.2f))
                                ) {
                                    if (state.currentUserPhotoUrl.isNotEmpty()) {
                                        AsyncImage(
                                            model = state.currentUserPhotoUrl,
                                            contentDescription = "Profil fotoğrafı",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .align(Alignment.Center),
                                            tint = primaryColor
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { viewModel.updateCommentText(it) },
                                    placeholder = { Text("Yorum yazın...") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        containerColor = surfaceColor,
                                        cursorColor = primaryColor
                                    ),
                                    maxLines = 3
                                )

                                IconButton(
                                    onClick = { viewModel.addComment() },
                                    enabled = commentText.isNotBlank() && !state.isCommentLoading
                                ) {
                                    if (state.isCommentLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                            color = primaryColor
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Send,
                                            contentDescription = "Gönder",
                                            tint = if (commentText.isBlank())
                                                textColor.copy(alpha = 0.38f) else
                                                primaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Gönderiyi Sil") },
            text = { Text("Bu gönderiyi silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePost()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB71C1C)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = primaryColor
                    )
                ) {
                    Text("İptal")
                }
            },
            containerColor = surfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun MediaGallery(
    mediaUrls: List<String>,
    mediaTypes: List<String>,
    onVideoClick: () -> Unit,
    videoControlsVisible: Boolean,
    context: Context
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
                    .clickable { if (isVideo) onVideoClick() }
            ) {
                if (isVideo) {
                    val videoUri = remember { Uri.parse(mediaUrl) }
                    val exoPlayer = remember {
                        ExoPlayer.Builder(context).build().apply {
                            setMediaItem(MediaItem.fromUri(videoUri))
                            prepare()
                        }
                    }

                    DisposableEffect(key1 = videoUri.toString()) {
                        onDispose {
                            exoPlayer.release()
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayer
                                useController = videoControlsVisible
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (!videoControlsVisible) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .align(Alignment.Center)
                                .background(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                } else {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = "Gönderi görüntüsü",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        mediaUrls.size in 2..4 -> {
            val rows = (mediaUrls.size + 1) / 2
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val startIndex = row * 2
                        val endIndex = minOf(startIndex + 2, mediaUrls.size)
                        for (i in startIndex until endIndex) {
                            val mediaUrl = mediaUrls[i]
                            val isVideo = mediaTypes.getOrNull(i) == "video"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = mediaUrl,
                                    contentDescription = "Gönderi görüntüsü",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (isVideo) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.Center)
                                            .background(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (row == rows - 1 && (endIndex - startIndex) < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
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
                        val mediaUrl = mediaUrls[i]
                        val isVideo = mediaTypes.getOrNull(i) == "video"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = mediaUrl,
                                contentDescription = "Gönderi görüntüsü",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (isVideo) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .align(Alignment.Center)
                                        .background(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
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
                            val mediaUrl = mediaUrls[i]
                            val isVideo = mediaTypes.getOrNull(i) == "video"
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = mediaUrl,
                                    contentDescription = "Gönderi görüntüsü",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (i == 4 && mediaUrls.size > 5) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.6f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+${mediaUrls.size - 5}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                } else if (isVideo) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .align(Alignment.Center)
                                            .background(
                                                color = Color.Black.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Video",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                if (mediaUrls.size > 5) {
                    Button(
                        onClick = { /* Tüm medyaları görüntüleme ekranı açılabilir */ },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tüm Medyaları Görüntüle (${mediaUrls.size})")
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    dateFormatter: SimpleDateFormat
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.2f))
                ) {
                    if (comment.userPhotoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = comment.userPhotoUrl,
                            contentDescription = "Profil fotoğrafı",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center),
                            tint = primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = comment.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = textColor
                    )
                    Text(
                        text = if (comment.userRole == "PHYSIOTHERAPIST") "Fizyoterapist" else "Kullanıcı",
                        fontSize = 12.sp,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = dateFormatter.format(comment.timestamp),
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comment.content,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = textColor
            )
        }
    }
}