package com.example.fizyoapp.presentation.social.feed

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.social.Post
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.ui.bottomnavbar.BottomNavbarComponent
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialFeedScreen(
    navController: NavController,
    viewModel: SocialFeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SocialFeedViewModel.SocialFeedUiEvent.NavigateToComments -> {
                    navController.navigate(AppScreens.CommentsScreen.createRoute(event.postId))
                }
                is SocialFeedViewModel.SocialFeedUiEvent.NavigateToProfile -> {
                    navController.navigate(AppScreens.SocialProfileScreen.createRoute(event.userId))
                }
                is SocialFeedViewModel.SocialFeedUiEvent.NavigateToCreatePost -> {
                    Log.d("SocialFeedScreen", "Navigating to CreatePostScreen")
                    navController.navigate(AppScreens.CreatePostScreen.route)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sosyal Medya",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.isPhysiotherapist) {
                FloatingActionButton(
                    onClick = {
                        Log.d("SocialFeedScreen", "FAB clicked")
                        viewModel.onEvent(SocialFeedEvent.NavigateToCreatePost)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Gönderi Oluştur")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            // Tab buttons for General/Following
            TabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = if (state.showFollowingFeed) 1 else 0
            ) {
                Tab(
                    selected = !state.showFollowingFeed,
                    onClick = { viewModel.onEvent(SocialFeedEvent.LoadGeneralPosts) },
                    text = { Text("Genel") }
                )
                Tab(
                    selected = state.showFollowingFeed,
                    onClick = { viewModel.onEvent(SocialFeedEvent.LoadFollowingPosts) },
                    text = { Text("Takip Ediliyor") }
                )
            }

            // Content area
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.errorMessage ?: "Bir hata oluştu")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            viewModel.onEvent(SocialFeedEvent.LoadGeneralPosts)
                        }) {
                            Text("Tekrar Dene")
                        }
                    }
                }
            } else {
                val posts = if (state.showFollowingFeed) state.followingPosts else state.generalPosts

                if (posts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (state.showFollowingFeed)
                                "Takip ettiğiniz kimse yok veya henüz gönderi paylaşmamışlar"
                            else
                                "Henüz gönderi bulunmuyor"
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp) // For FAB
                    ) {
                        items(posts) { post ->
                            PostItem(
                                post = post,
                                onLikeClick = { viewModel.onEvent(SocialFeedEvent.ToggleLike(post.id)) },
                                onCommentClick = { viewModel.onEvent(SocialFeedEvent.ShowComments(post.id)) },
                                onProfileClick = { viewModel.onEvent(SocialFeedEvent.NavigateToProfile(post.authorId)) },
                                onFollowClick = { viewModel.onEvent(SocialFeedEvent.ToggleFollow(post.authorId)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd MMM yy, HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Author info and follow button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile photo placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable(onClick = onProfileClick),
                    contentAlignment = Alignment.Center
                ) {
                    // In a real app, load the author's profile photo here
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Photo",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Author name and timestamp
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onProfileClick)
                ) {
                    Text(
                        text = "FZT. [Author Name]", // In a real app, get from FireStore
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormatter.format(post.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Follow button
                OutlinedButton(
                    onClick = onFollowClick,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(text = "Takip Et") // Should change to "Takibi Bırak" if already following
                }
            }

            // Post content
            Text(
                text = post.content,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Post media - if any
            if (post.mediaUrls.isNotEmpty()) {
                AsyncImage(
                    model = post.mediaUrls.first(),
                    contentDescription = "Post Media",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Like and comment buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Like button
                Row(
                    modifier = Modifier
                        .clickable(onClick = onLikeClick)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.isLikedByCurrentUser) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLikedByCurrentUser) Color.Red else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.likeCount.toString(),
                        color = Color.Gray
                    )
                }

                // Comment button
                Row(
                    modifier = Modifier
                        .clickable(onClick = onCommentClick)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = post.commentCount.toString(),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}