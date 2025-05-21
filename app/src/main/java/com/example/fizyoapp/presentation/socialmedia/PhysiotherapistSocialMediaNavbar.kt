// presentation/socialmedia/PhysiotherapistSocialMediaNavbar.kt
package com.example.fizyoapp.presentation.socialmedia

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.navigation.AppScreens

@Composable
fun PhysiotherapistSocialMediaNavbar(
    navController: NavController,
    currentRoute: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                title = "Ana Sayfa",
                isSelected = currentRoute == AppScreens.SocialMediaScreen.route,
                onClick = {
                    if (currentRoute != AppScreens.SocialMediaScreen.route) {
                        navController.navigate(AppScreens.SocialMediaScreen.route) {
                            popUpTo(AppScreens.SocialMediaScreen.route) { inclusive = true }
                        }
                    }
                }
            )

            NavBarItem(
                icon = Icons.Default.Search,
                title = "Ara",
                isSelected = currentRoute == AppScreens.SocialMediaSearchScreen.route,
                onClick = {
                    if (currentRoute != AppScreens.SocialMediaSearchScreen.route) {
                        navController.navigate(AppScreens.SocialMediaSearchScreen.route) {
                            launchSingleTop = true
                        }
                    }
                }
            )

            NavBarItem(
                icon = Icons.Default.Add,
                title = "Paylaş",
                isSelected = currentRoute == AppScreens.CreatePostScreen.route,
                onClick = {
                    navController.navigate(AppScreens.CreatePostScreen.route) {
                        launchSingleTop = true
                    }
                }
            )

            NavBarItem(
                icon = Icons.Default.Person,
                title = "Profil",
                isSelected = currentRoute.startsWith(AppScreens.PhysiotherapistSocialProfile.route),
                onClick = {
                    navController.navigate(AppScreens.PhysiotherapistSocialProfile.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}