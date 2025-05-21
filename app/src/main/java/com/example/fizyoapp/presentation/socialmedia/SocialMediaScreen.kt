// presentation/socialmedia/SocialMediaScreen.kt
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.auth.UserRole
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.example.fizyoapp.presentation.navigation.AppScreens
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
    val userProfileState by viewModel.userProfile.collectAsState()
    val physiotherapistProfileState by viewModel.physiotherapistProfile.collectAsState()
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""
    val isPhysiotherapist = currentUser?.role == UserRole.PHYSIOTHERAPIST

    // Kullanıcı adı ve soyadı - Yerel değişkenlere atayarak smart cast sorununu çöz
    val userProfile = userProfileState
    val physiotherapistProfile = physiotherapistProfileState

    val userFullName = when {
        isPhysiotherapist && physiotherapistProfile != null -> {
            "${physiotherapistProfile.firstName} ${physiotherapistProfile.lastName}"
        }
        !isPhysiotherapist && userProfile != null -> {
            "${userProfile.firstName} ${userProfile.lastName}"
        }
        else -> "Kullanıcı"
    }

    // Load posts and user info when the screen appears
    LaunchedEffect(key1 = true) {
        viewModel.loadPosts()
        viewModel.loadUserInfo()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Merhaba, $userFullName")
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    if (isPhysiotherapist) {
                        // Create post button for physiotherapists
                        IconButton(onClick = { navController.navigate(AppScreens.CreatePostScreen.route) }) {
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
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.posts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz gönderi paylaşılmamış",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(state.posts) { post ->
                        PostItem(
                            post = post,
                            onClickDetail = {
                                navController.navigate(AppScreens.PostDetailScreen.createRoute(post.id))
                            },
                            onLike = {
                                viewModel.onLikePost(post.id)
                            },
                            isLikedByCurrentUser = post.likedBy.contains(currentUser?.id),
                            isCurrentUserPost = post.userId == currentUser?.id
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
            // Error handling
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
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
    onClickDetail: () -> Unit,
    onLike: () -> Unit,
    isLikedByCurrentUser: Boolean,
    isCurrentUserPost: Boolean
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

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
            // User info
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

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = post.userName,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (post.userRole == "PHYSIOTHERAPIST") "Fizyoterapist" else "Kullanıcı",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (post.userRole == "PHYSIOTHERAPIST")
                            MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = dateFormat.format(post.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge
            )

            // Show first media if exists
            if (post.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                AsyncImage(
                    model = post.mediaUrls.first(),
                    contentDescription = "Gönderi görüntüsü",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                if (post.mediaUrls.size > 1) {
                    Text(
                        text = "+${post.mediaUrls.size - 1} daha fazla",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions
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
                        tint = if (isLikedByCurrentUser) Color.Red else Color.Gray
                    )
                }

                Text(
                    text = "${post.likeCount} beğeni",
                    style = MaterialTheme.typography.bodySmall,
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
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { onClickDetail() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Detaylar")
                }
            }
        }
    }
}