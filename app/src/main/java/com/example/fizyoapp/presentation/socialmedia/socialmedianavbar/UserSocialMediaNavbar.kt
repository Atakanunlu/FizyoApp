package com.example.fizyoapp.presentation.socialmedia.socialmedianavbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fizyoapp.presentation.navigation.AppScreens

private val primaryColor = Color(59, 62, 104)
private val surfaceColor = Color.White

@Composable
fun UserSocialMediaNavbar(
    navController: NavController,
    currentRoute: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        color = surfaceColor,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navController.navigate(AppScreens.SocialMediaScreen.route) {
                            popUpTo(AppScreens.SocialMediaScreen.route) { inclusive = true }
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Ana Sayfa",
                    tint = if (currentRoute == AppScreens.SocialMediaScreen.route)
                        primaryColor
                    else
                        Color.Gray,
                    modifier = Modifier.size(26.dp)
                )

                if (currentRoute == AppScreens.SocialMediaScreen.route) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ana Sayfa",
                        color = primaryColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navController.navigate(AppScreens.SocialMediaSearchScreen.route)
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.PersonSearch,
                    contentDescription = "Fizyoterapist Ara",
                    tint = if (currentRoute == AppScreens.SocialMediaSearchScreen.route)
                        primaryColor
                    else
                        Color.Gray,
                    modifier = Modifier.size(26.dp)
                )

                if (currentRoute == AppScreens.SocialMediaSearchScreen.route) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ara",
                        color = primaryColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}