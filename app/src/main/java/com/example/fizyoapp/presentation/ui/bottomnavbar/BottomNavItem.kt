package com.example.fizyoapp.ui.bottomnavbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fizyoapp.presentation.navigation.AppScreens

data class BottomNavItem(
    val title: String,
    val selectedicon: ImageVector,
    val unselectedicon: ImageVector,
    val route: String
)

val items = listOf(
    BottomNavItem(
        title = "Home",
        selectedicon = Icons.Default.Home,
        unselectedicon = Icons.Default.Home,
        route = AppScreens.UserMainScreen.route
    ),
    BottomNavItem(
        title = "Search",
        selectedicon = Icons.Default.Search,
        unselectedicon = Icons.Default.Search,
        route = AppScreens.SearchScreen.route
    ),
    BottomNavItem(
        title = "Mesajlar",
        selectedicon = Icons.Default.Message,
        unselectedicon = Icons.Default.Message,
        route = AppScreens.MessagesScreen.route
    )
)