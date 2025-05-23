package com.example.fizyoapp.presentation.socialmedia.socialmedianavbar

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.navigation.AppScreens
import com.example.fizyoapp.presentation.socialmedia.notification.NotificationCountViewModel

@Composable
fun PhysiotherapistSocialMediaNavbar(
    navController: NavController,
    currentRoute: String,
    notificationViewModel: NotificationCountViewModel = hiltViewModel()
) {
    val unreadCount by notificationViewModel.unreadNotificationsCount.collectAsState()

    Surface(
        modifier = Modifier.fillMaxWidth().height(70.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                title = "Ana Sayfa",
                isSelected = currentRoute == AppScreens.SocialMediaScreen.route,
                onClick = {
                    navController.navigate(AppScreens.SocialMediaScreen.route) {
                        popUpTo(AppScreens.SocialMediaScreen.route) { inclusive = true }
                    }
                }
            )
            NavBarItem(
                icon = Icons.Default.Search,
                title = "Ara",
                isSelected = currentRoute == AppScreens.SocialMediaSearchScreen.route,
                onClick = {
                    navController.navigate(AppScreens.SocialMediaSearchScreen.route)
                }
            )
            NavBarItem(
                icon = Icons.Default.Add,
                title = "Paylaş",
                isSelected = currentRoute == AppScreens.CreatePostScreen.route,
                onClick = {
                    navController.navigate(AppScreens.CreatePostScreen.route)
                }
            )

            Box {
                NavBarItem(
                    icon = Icons.Default.Notifications,
                    title = "Bildirimler",
                    isSelected = currentRoute == AppScreens.NotificationScreen.route,
                    onClick = {
                        navController.navigate(AppScreens.NotificationScreen.route)
                    }
                )

                if (unreadCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-5).dp, y = 5.dp)
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            NavBarItem(
                icon = Icons.Default.Person,
                title = "Profil",
                isSelected = currentRoute.startsWith(AppScreens.PhysiotherapistSocialProfile.route),
                onClick = {
                    navController.navigate(AppScreens.PhysiotherapistSocialProfile.route)
                }
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedSize by animateDpAsState(
        targetValue = if (isSelected) 48.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "size"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(animatedSize)
                .clip(CircleShape)
                .background(
                    if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }

        if (isSelected) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}