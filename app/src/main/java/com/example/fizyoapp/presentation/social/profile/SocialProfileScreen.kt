package com.example.fizyoapp.presentation.social.profile

import android.annotation.SuppressLint
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.social.Post
import com.example.fizyoapp.presentation.navigation.AppScreens
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialProfileScreen(
    navController: NavController,
    viewModel: SocialProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is SocialProfileViewModel.SocialProfileUiEvent.NavigateToComments -> {
                    navController.navigate(AppScreens.CommentsScreen.createRoute(event.postId))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
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
                Text(text = state.errorMessage!!)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                item {
                    ProfileHeader(
                        name = state.profile?.name ?: "",
                        photoUrl = state.profile?.photoUrl ?: "",
                        followersCount = state.profile?.followersCount ?: 0,
                        followingCount = state.profile?.followingCount ?: 0,
                        isFollowed = state.profile?.isFollowedByCurrentUser ?: false,
                        isCurrentUser = state.isCurrentUser,
                        onFollowClick = { viewModel.onEvent(SocialProfileEvent.ToggleFollow) }
                    )
                }

                if (state.posts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Henüz gönderi bulunmuyor")
                        }
                    }
                } else {
                    items(state.posts) { post ->
                        ProfilePostItem(
                            post = post,
                            onLikeClick = { viewModel.onEvent(SocialProfileEvent.ToggleLike(post.id)) },
                            onCommentClick = { viewModel.onEvent(SocialProfileEvent.ShowComments(post.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    name: String,
    photoUrl: String,
    followersCount: Int,
    followingCount: Int,
    isFollowed: Boolean,
    isCurrentUser: Boolean,
    onFollowClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile photo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Photo",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Name and follow button
            Column {
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!isCurrentUser) {
                    Button(
                        onClick = onFollowClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowed) Color.LightGray else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = if (isFollowed) "Takibi Bırak" else "Takip Et")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Followers and following counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = followersCount.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(text = "Takipçi")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = followingCount.toString(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(text = "Takip Edilen")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
    }
}

@Composable
fun ProfilePostItem(
    post: Post,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
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
            // Date
            Text(
                text = dateFormatter.format(post.createdAt),
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

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