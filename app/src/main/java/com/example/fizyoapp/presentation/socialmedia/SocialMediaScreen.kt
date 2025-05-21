// presentation/socialmedia/SocialMediaScreen.kt
package com.example.fizyoapp.presentation.socialmedia

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.bottomnavbar.BottomNavbarComponent
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaScreen(
    navController: NavController,
    viewModel: SocialMediaViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // Kullanıcı rolünü kontrol et
    val isPhysiotherapist = currentUser?.role?.name == "PHYSIOTHERAPIST"

    LaunchedEffect(key1 = true) {
        viewModel.loadPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fizyoapp Sosyal Medya") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (isPhysiotherapist) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(AppScreens.CreatePostScreen.route)
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Gönderi Ekle",
                        tint = Color.White
                    )
                }
            }
        },
        // Sadece kullanıcı rolündekiler için bottomBar göster
        bottomBar = {
            if (!isPhysiotherapist) {
                BottomNavbarComponent(navController)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(bottom = if (!isPhysiotherapist) paddingValues.calculateBottomPadding() else 0.dp)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error ?: "Bir hata oluştu",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadPosts() }) {
                        Text("Tekrar Dene")
                    }
                }
            } else if (state.posts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Henüz gönderi paylaşılmamıştır",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 60.dp) // BottomNavigation için boşluk
                ) {
                    items(state.posts) { post ->
                        PostItem(
                            post = post,
                            currentUserId = currentUser?.id ?: "",
                            onPostClick = { navController.navigate(AppScreens.PostDetailScreen.createRoute(post.id)) },
                            onLikeClick = { viewModel.onLikePost(post.id) }
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun PostItem(
    post: Post,
    currentUserId: String,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val isLiked = post.likedBy.contains(currentUserId)
    var videoControlsVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onPostClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Kullanıcı bilgisi
            Row(
                verticalAlignment = Alignment.CenterVertically
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
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = post.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = if (post.userRole == "PHYSIOTHERAPIST") "Fizyoterapist" else "Kullanıcı",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = dateFormatter.format(post.timestamp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Gönderi içeriği
            Text(
                text = post.content,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Medya içeriği varsa göster
            if (post.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                // Medya sayısına göre farklı görünümler
                when {
                    // Tek medya varsa
                    post.mediaUrls.size == 1 -> {
                        val mediaUrl = post.mediaUrls.first()
                        val isVideo = post.mediaTypes.firstOrNull() == "video"

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { videoControlsVisible = !videoControlsVisible }
                        ) {
                            if (isVideo) {
                                // Video Player
                                val context = LocalContext.current
                                val videoUri = remember { Uri.parse(mediaUrl) }
                                val exoPlayer = remember {
                                    ExoPlayer.Builder(context).build().apply {
                                        setMediaItem(MediaItem.fromUri(videoUri))
                                        prepare()
                                    }
                                }

                                DisposableEffect(Unit) {
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

                                // Video play butonu
                                if (!videoControlsVisible) {
                                    Box(
                                        modifier = Modifier
                                            .size(50.dp)
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
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                            } else {
                                // Resim gösterimi
                                AsyncImage(
                                    model = mediaUrl,
                                    contentDescription = "Gönderi görüntüsü",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    // 2 medya varsa yan yana göster
                    post.mediaUrls.size == 2 -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            post.mediaUrls.forEachIndexed { index, mediaUrl ->
                                val isVideo = post.mediaTypes.getOrNull(index) == "video"

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
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
                    }

                    // 3 medya varsa 1 büyük, 2 küçük göster
                    post.mediaUrls.size == 3 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // İlk medya (büyük)
                            val firstMediaUrl = post.mediaUrls.first()
                            val isFirstVideo = post.mediaTypes.firstOrNull() == "video"

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = firstMediaUrl,
                                    contentDescription = "Gönderi görüntüsü",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                if (isFirstVideo) {
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

                            // Diğer iki medya (yan yana)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                post.mediaUrls.drop(1).forEachIndexed { index, mediaUrl ->
                                    val isVideo = post.mediaTypes.getOrNull(index + 1) == "video"

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
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
                                }
                            }
                        }
                    }

                    // 4 veya daha fazla medya için ızgara görünümü
                    post.mediaUrls.size >= 4 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // İlk 4 medyayı 2x2 grid olarak göster
                            for (i in 0..1) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    for (j in 0..1) {
                                        val index = i * 2 + j
                                        if (index < post.mediaUrls.size) {
                                            val mediaUrl = post.mediaUrls[index]
                                            val isVideo = post.mediaTypes.getOrNull(index) == "video"

                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(8.dp))
                                            ) {
                                                AsyncImage(
                                                    model = mediaUrl,
                                                    contentDescription = "Gönderi görüntüsü",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )

                                                // Eğer 4'ten fazla medya varsa ve son hücreyse, kalan sayıyı göster
                                                if (index == 3 && post.mediaUrls.size > 4) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .background(Color.Black.copy(alpha = 0.5f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "+${post.mediaUrls.size - 4}",
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 20.sp
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
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Like ve Comment sayıları
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onLikeClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Beğen",
                        tint = if (isLiked) Color.Red else Color.Gray
                    )
                }

                Text(
                    text = post.likeCount.toString(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "Yorum",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = post.commentCount.toString(),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
