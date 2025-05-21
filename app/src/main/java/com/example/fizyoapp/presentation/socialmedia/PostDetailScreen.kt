// presentation/socialmedia/PostDetailScreen.kt
package com.example.fizyoapp.presentation.socialmedia

import android.net.Uri
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

    // Diyalog durumları
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    // Burada context'i ekliyoruz
    val context = LocalContext.current

    LaunchedEffect(key1 = state.postDeleted) {
        if (state.postDeleted) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gönderi Detayı") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    // Gönderi sahibiyse düzenleme ve silme seçenekleri göster
                    if (state.post?.userId == state.currentUserId) {
                        IconButton(onClick = { showDeleteConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Gönderiyi Sil"
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
                                contentDescription = "Gönderiyi Düzenle"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        text = state.error ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadPostAndComments() }) {
                        Text("Tekrar Dene")
                    }
                }
            } else if (state.post == null) {
                Text(
                    text = "Gönderi bulunamadı",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val post = state.post // Local değişkene atama yaparak smart cast
                if (post != null) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // İçerik alanı
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            // Gönderi detayı
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
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

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Gönderi içeriği
                                        Text(
                                            text = post.content,
                                            modifier = Modifier.fillMaxWidth(),
                                            fontSize = 16.sp
                                        )

                                        // PostDetailScreen.kt içinde, gönderi detayı içindeki medya görüntüleme kısmı

// Medya içeriği varsa göster
                                        if (post.mediaUrls.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Medya sayısına göre farklı görünümler
                                            when {
                                                // Tek medya varsa
                                                post.mediaUrls.size == 1 -> {
                                                    val mediaUrl = post.mediaUrls.first()
                                                    val isVideo = post.mediaTypes.firstOrNull() == "video"

                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(280.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .clickable { videoControlsVisible = !videoControlsVisible }
                                                    ) {
                                                        if (isVideo) {
                                                            // Video Player
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

                                                // 2-4 arası medya için grid görünümü
                                                post.mediaUrls.size in 2..4 -> {
                                                    val rows = (post.mediaUrls.size + 1) / 2  // 2-3 medya için 2 satır, 4 medya için 2 satır

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
                                                                val endIndex = minOf(startIndex + 2, post.mediaUrls.size)

                                                                for (i in startIndex until endIndex) {
                                                                    val mediaUrl = post.mediaUrls[i]
                                                                    val isVideo = post.mediaTypes.getOrNull(i) == "video"

                                                                    Box(
                                                                        modifier = Modifier
                                                                            .weight(1f)
                                                                            .fillMaxHeight()
                                                                            .clip(RoundedCornerShape(8.dp))
                                                                            .clickable {
                                                                                // Burada seçilen medyayı tam ekran göstermek için bir işlev ekleyebilirsiniz
                                                                            }
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

                                                                // Satırı doldurmak için gereken boş hücreler
                                                                if (row == rows - 1 && (endIndex - startIndex) < 2) {
                                                                    repeat(2 - (endIndex - startIndex)) {
                                                                        Spacer(modifier = Modifier.weight(1f))
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                // 5 veya daha fazla medya için gösterim
                                                else -> {
                                                    Column(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        // İlk satır - 2 medya yan yana
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(150.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            for (i in 0..1) {
                                                                val mediaUrl = post.mediaUrls[i]
                                                                val isVideo = post.mediaTypes.getOrNull(i) == "video"

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

                                                        // İkinci satır - 3 medya yan yana
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(100.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            for (i in 2..4) {
                                                                if (i < post.mediaUrls.size) {
                                                                    val mediaUrl = post.mediaUrls[i]
                                                                    val isVideo = post.mediaTypes.getOrNull(i) == "video"

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

                                                                        if (i == 4 && post.mediaUrls.size > 5) {
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .fillMaxSize()
                                                                                    .background(Color.Black.copy(alpha = 0.5f)),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                Text(
                                                                                    text = "+${post.mediaUrls.size - 5}",
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
                                                                } else {
                                                                    Spacer(modifier = Modifier.weight(1f))
                                                                }
                                                            }
                                                        }

                                                        // Daha fazla medya gösterme seçeneği
                                                        if (post.mediaUrls.size > 5) {
                                                            TextButton(
                                                                onClick = { /* Tüm medyaları görüntüleme ekranı açılabilir */ },
                                                                modifier = Modifier.align(Alignment.End)
                                                            ) {
                                                                Text("Tüm Medyaları Görüntüle (${post.mediaUrls.size})")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Beğeni ve yorum sayıları
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
                                                        Color.Gray
                                                )
                                            }

                                            Text(
                                                text = "${post.likeCount} beğeni",
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
                                                text = "${post.commentCount} yorum",
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }

                            // Yorumlar başlığı
                            item {
                                Text(
                                    text = "Yorumlar",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            // Yorumlar listesi
                            if (state.comments.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Henüz yorum yapılmamış",
                                            color = Color.Gray
                                        )
                                    }
                                }
                            } else {
                                items(state.comments) { comment ->
                                    CommentItem(comment = comment, dateFormatter = dateFormatter)
                                }
                            }

                            // Alt kısımda yorum giriş alanı için boşluk
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }

                        // Yorum giriş alanı
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
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
                                            tint = MaterialTheme.colorScheme.primary
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
                                        unfocusedBorderColor = Color.Transparent
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
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Send,
                                            contentDescription = "Gönder",
                                            tint = if (commentText.isBlank())
                                                Color.Gray else
                                                MaterialTheme.colorScheme.primary
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

    // Silme onay diyalogu
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
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("İptal")
                }
            }
        )
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
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Kullanıcı bilgisi
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
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
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = comment.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (comment.userRole == "PHYSIOTHERAPIST") "Fizyoterapist" else "Kullanıcı",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = dateFormatter.format(comment.timestamp),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Yorum içeriği
            Text(
                text = comment.content,
                fontSize = 14.sp
            )
        }
    }
}