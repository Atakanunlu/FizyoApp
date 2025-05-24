package com.example.fizyoapp.presentation.socialmedia.notification

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
import com.example.fizyoapp.domain.model.notification.NotificationType
import com.example.fizyoapp.domain.model.notification.SocialMediaNotification
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.socialmedia.socialmedianavbar.PhysiotherapistSocialMediaNavbar
import java.text.SimpleDateFormat
import java.util.*

private val primaryColor = Color(59, 62, 104)
private val backgroundColor = Color(245, 245, 250)
private val surfaceColor = Color.White
private val textColor = Color.DarkGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val currentRoute = navController.currentBackStackEntry?.destination?.route ?: ""

    LaunchedEffect(key1 = true) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Bildirimler",
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
                    if (state.notifications.any { !it.isRead }) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Tümünü Okundu İşaretle",
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
            } else if (state.notifications.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = primaryColor.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz bildiriminiz yok",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.notifications) { notification ->
                        NotificationItem(
                            notification = notification,
                            onClick = {
                                viewModel.markAsRead(notification.id)
                                when (notification.type) {
                                    NotificationType.LIKE, NotificationType.COMMENT -> {
                                        navController.navigate(AppScreens.PostDetailScreen.createRoute(notification.contentId))
                                    }
                                    NotificationType.FOLLOW -> {
                                        navController.navigate("${AppScreens.PhysiotherapistSocialProfile.route}/${notification.senderId}")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = Color(0xFFB71C1C),
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
fun NotificationItem(
    notification: SocialMediaNotification,
    onClick: () -> Unit,
    viewModel: NotificationItemViewModel = hiltViewModel()
) {
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    val senderProfile by viewModel.senderProfile.collectAsState()

    LaunchedEffect(notification.senderId) {
        viewModel.loadSenderProfile(notification.senderId, notification.senderRole)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!notification.isRead)
                primaryColor.copy(alpha = 0.1f)
            else
                surfaceColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.1f))
            ) {
                if (senderProfile?.photoUrl?.isNotEmpty() == true) {
                    AsyncImage(
                        model = senderProfile?.photoUrl,
                        contentDescription = "Profil fotoğrafı",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center),
                        tint = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val senderName = senderProfile?.name ?: "Bir kullanıcı"
                val message = when (notification.type) {
                    NotificationType.LIKE -> "paylaşımınızı beğendi"
                    NotificationType.COMMENT -> "paylaşımınıza yorum yaptı"
                    NotificationType.FOLLOW -> "sizi takip etmeye başladı"
                }

                Text(
                    text = "$senderName $message",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (notification.type != NotificationType.FOLLOW && notification.contentText.isNotEmpty()) {
                    Text(
                        text = notification.contentText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = dateFormat.format(notification.timestamp),
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.5f)
                )
            }

            Icon(
                imageVector = when (notification.type) {
                    NotificationType.LIKE -> Icons.Default.Favorite
                    NotificationType.COMMENT -> Icons.AutoMirrored.Filled.Comment
                    NotificationType.FOLLOW -> Icons.Default.Person
                },
                contentDescription = null,
                tint = when (notification.type) {
                    NotificationType.LIKE -> Color.Red
                    NotificationType.COMMENT -> primaryColor
                    NotificationType.FOLLOW -> primaryColor
                },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}