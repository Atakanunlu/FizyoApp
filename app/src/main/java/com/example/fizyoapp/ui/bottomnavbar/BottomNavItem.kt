package com.example.fizyoapp.ui.bottomnavbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SendToMobile
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title :String,
    val selectedicon:ImageVector,
    val unselectedicon:ImageVector,
    val route:String
)

val items = listOf(

    BottomNavItem(
        title = "Home",
        selectedicon = Icons.Default.Home,
        unselectedicon = Icons.Filled.Home,
        route="main_screen"
    ),

    BottomNavItem(
        title = "Search",
        selectedicon = Icons.Default.Search,
        unselectedicon = Icons.Filled.Search,
        route="search_screen"
    ),

    BottomNavItem(
        title = "Paylaşımlar",
        selectedicon = Icons.Default.SendToMobile,
        unselectedicon = Icons.Filled.Send,
        route="main_screen"
    ),

    BottomNavItem(
        title = "Profil",
        selectedicon = Icons.Default.PersonPin,
        unselectedicon = Icons.Filled.PersonPin,
        route="main_screen"
    ),
    BottomNavItem(
        title = "Mesajlar",
        selectedicon = Icons.Default.Message,
        unselectedicon = Icons.Filled.Message,
        route="main_screen"
    )




)

