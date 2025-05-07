package com.example.fizyoapp.presentation.ui.bottomnavbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SendToMobile
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
        unselectedicon = Icons.Filled.Home,
        route = AppScreens.UserMainScreen.route
    ),
    BottomNavItem(
        title = "Search",
        selectedicon = Icons.Default.Search,
        unselectedicon = Icons.Filled.Search,
        route = AppScreens.SearchScreen.route
    ),
    BottomNavItem(
        title = "Paylaşımlar",
        selectedicon = Icons.Default.SendToMobile,
        unselectedicon = Icons.Filled.Send,
        route = AppScreens.PaylasimlarScreen.route
    ),
    BottomNavItem(
        title = "Profil",
        selectedicon = Icons.Default.PersonPin,
        unselectedicon = Icons.Filled.PersonPin,
        route = AppScreens.ProfilScreen.route
    ),
    BottomNavItem(
        title = "Mesajlar",
        selectedicon = Icons.Default.Message,
        unselectedicon = Icons.Filled.Message,
        route = AppScreens.UserMainScreen.route
    )
)