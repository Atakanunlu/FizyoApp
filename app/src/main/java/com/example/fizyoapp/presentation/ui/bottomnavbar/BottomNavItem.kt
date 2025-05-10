package com.example.fizyoapp.ui.bottomnavbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
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
        title = "Ana Sayfa",
        selectedicon = Icons.Filled.Home,
        unselectedicon = Icons.Outlined.Home,
        route = AppScreens.UserMainScreen.route
    ),
    BottomNavItem(
        title = "Keşfet",
        selectedicon = Icons.Filled.Search,
        unselectedicon = Icons.Outlined.Search,
        route = AppScreens.SearchScreen.route
    ),
    BottomNavItem(
        title = "Paylaşım",
        selectedicon = Icons.Filled.Share,
        unselectedicon = Icons.Outlined.Share,
        route = AppScreens.PaylasimlarScreen.route
    ),
    BottomNavItem(
        title = "Profil",
        selectedicon = Icons.Filled.Person,
        unselectedicon = Icons.Outlined.Person,
        route = AppScreens.ProfilScreen.route
    ),
    BottomNavItem(
        title = "Mesajlar",
        selectedicon = Icons.Filled.Message,
        unselectedicon = Icons.Outlined.Message,
        route = AppScreens.MessagesScreen.route
    )
)