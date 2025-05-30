package com.example.fizyoapp.presentation.socialmedia.physiotherapistsocialprofile

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Feed
import androidx.compose.material.icons.automirrored.filled.Message
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.fizyoapp.domain.model.follow.FollowRelation
import com.example.fizyoapp.domain.model.physiotherapist_profile.PhysiotherapistProfile
import com.example.fizyoapp.domain.model.socialmedia.Post
import com.example.fizyoapp.domain.model.user_profile.UserProfile
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.socialmedia.socialmedianavbar.PhysiotherapistSocialMediaNavbar
import com.example.fizyoapp.presentation.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysiotherapistSocialProfileScreen(
    navController: NavController,
    physiotherapistId: String? = null,
    viewModel: PhysiotherapistSocialProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Fizyoterapist Profili",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Yenile",
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
            PhysiotherapistSocialMediaNavbar(navController, currentRoute)
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
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = surfaceColor)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val profile = state.profile
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.1f))
                                ) {
                                    if (profile?.profilePhotoUrl?.isNotEmpty() == true) {
                                        AsyncImage(
                                            model = profile.profilePhotoUrl,
                                            contentDescription = "Profil fotoğrafı",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(60.dp)
                                                .align(Alignment.Center),
                                            tint = primaryColor
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "${profile?.firstName ?: ""} ${profile?.lastName ?: ""}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = "Fizyoterapist",
                                    fontSize = 16.sp,
                                    color = primaryColor
                                )
                                if (physiotherapistId != null && physiotherapistId != currentUser?.id) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FollowButton(
                                            isFollowing = state.isFollowing,
                                            isLoading = state.isFollowLoading,
                                            onClick = { viewModel.toggleFollow(physiotherapistId) },
                                            modifier = Modifier.width(130.dp)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        MessageButton(
                                            onClick = {
                                                navController.navigate(
                                                    AppScreens.MessagesDetailScreen.createMessageDetailRoute(physiotherapistId)
                                                )
                                            },
                                            modifier = Modifier.width(130.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                ProfileStats(
                                    postCount = state.posts.size,
                                    followersCount = state.followersCount,
                                    followingCount = state.followingCount,
                                    onFollowersClick = { viewModel.toggleShowFollowers() },
                                    onFollowingClick = { viewModel.toggleShowFollowing() }
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            text = "Paylaşımlar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)
                        )
                    }
                    if (state.posts.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceColor)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Feed,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = primaryColor.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Henüz paylaşım yapılmamış",
                                            color = textColor.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        items(state.posts) { post ->
                            ProfilePostItem(
                                post = post,
                                onClick = {
                                    navController.navigate(AppScreens.PostDetailScreen.createRoute(post.id))
                                }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
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
            if (state.showFollowers) {
                FollowersDialog(
                    followers = state.followers,
                    profiles = state.followerProfiles,
                    onDismiss = { viewModel.toggleShowFollowers() },
                    onProfileClick = { userId, role ->
                        if (role == "PHYSIOTHERAPIST") {
                            navController.navigate("${AppScreens.PhysiotherapistSocialProfile.route}/$userId")
                        }
                        viewModel.toggleShowFollowers()
                    }
                )
            }
            if (state.showFollowing) {
                FollowingDialog(
                    following = state.following,
                    profiles = state.followingProfiles,
                    onDismiss = { viewModel.toggleShowFollowing() },
                    onProfileClick = { userId ->
                        navController.navigate("${AppScreens.PhysiotherapistSocialProfile.route}/$userId")
                        viewModel.toggleShowFollowing()
                    }
                )
            }
        }
    }
}

@Composable
fun MessageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, primaryColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = primaryColor
        ),
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Message,
            contentDescription = "Mesaj Gönder",
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Mesaj Gönder",
            fontSize = 12.sp
        )
    }
}

@Composable
fun ProfileStats(
    postCount: Int,
    followersCount: Int,
    followingCount: Int,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(title = "Paylaşımlar", value = postCount.toString())
        StatItem(
            title = "Takipçiler",
            value = followersCount.toString(),
            onClick = onFollowersClick
        )
        StatItem(
            title = "Takip Edilen",
            value = followingCount.toString(),
            onClick = onFollowingClick
        )
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Text(
            text = title,
            fontSize = 14.sp,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ProfilePostItem(
    post: Post,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = post.content,
                color = textColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (post.mediaUrls.isNotEmpty()) {
                AsyncImage(
                    model = post.mediaUrls.first(),
                    contentDescription = "Post media",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                if (post.mediaUrls.size > 1) {
                    Text(
                        text = "+${post.mediaUrls.size - 1} daha fazla",
                        fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.End),
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = errorColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.likeCount} beğeni",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp),
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "${post.commentCount} yorum",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp),
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = dateFormat.format(post.timestamp),
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.6f)
                )
            }
            Button(
                onClick = { onClick() },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                )
            ) {
                Text("Detaylar")
            }
        }
    }
}

@Composable
fun FollowButton(
    isFollowing: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        } else {
            if (isFollowing) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Takip Ediliyor",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Takip Ediliyor",
                    fontSize = 12.sp
                )
            } else {
                Text(
                    text = "Takip Et",
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun FollowersDialog(
    followers: List<FollowRelation>,
    profiles: Map<String, Any>,
    onDismiss: () -> Unit,
    onProfileClick: (String, String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Takipçiler",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (followers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz takipçi yok",
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(followers) { follower ->
                            val profile = profiles[follower.followerId]
                            val name: String
                            val photoUrl: String
                            val role = follower.followerRole
                            when (profile) {
                                is PhysiotherapistProfile -> {
                                    name = "${profile.firstName} ${profile.lastName}"
                                    photoUrl = profile.profilePhotoUrl
                                }
                                is UserProfile -> {
                                    name = "${profile.firstName} ${profile.lastName}"
                                    photoUrl = profile.profilePhotoUrl
                                }
                                else -> {
                                    name = "Bilinmeyen Kullanıcı"
                                    photoUrl = ""
                                }
                            }
                            FollowProfileItem(
                                name = name,
                                photoUrl = photoUrl,
                                role = role,
                                onClick = { onProfileClick(follower.followerId, role) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    )
                ) {
                    Text("Kapat")
                }
            }
        }
    }
}

@Composable
fun FollowingDialog(
    following: List<FollowRelation>,
    profiles: Map<String, PhysiotherapistProfile>,
    onDismiss: () -> Unit,
    onProfileClick: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = surfaceColor
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Takip Edilenler",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (following.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz takip edilen fizyoterapist yok",
                            color = textColor.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(following) { follow ->
                            val profile = profiles[follow.followedId]
                            if (profile != null) {
                                FollowProfileItem(
                                    name = "${profile.firstName} ${profile.lastName}",
                                    photoUrl = profile.profilePhotoUrl,
                                    role = "PHYSIOTHERAPIST",
                                    onClick = { onProfileClick(follow.followedId) }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    )
                ) {
                    Text("Kapat")
                }
            }
        }
    }
}

@Composable
fun FollowProfileItem(
    name: String,
    photoUrl: String,
    role: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = role == "PHYSIOTHERAPIST") { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(primaryColor.copy(alpha = 0.1f))
        ) {
            if (photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = photoUrl,
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
                text = name,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Text(
                text = if (role == "PHYSIOTHERAPIST") "Fizyoterapist" else "Kullanıcı",
                fontSize = 12.sp,
                color = if (role == "PHYSIOTHERAPIST")
                    primaryColor
                else
                    textColor.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (role == "PHYSIOTHERAPIST") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Profili Görüntüle",
                tint = primaryColor.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}