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
    val route:String,
    val selectedicon:ImageVector,
    val unselectedicon:ImageVector
)

val items = listOf(

    BottomNavItem(
        title = "Home",
        route = "main_Screen",
        selectedicon = Icons.Default.Home,
        unselectedicon = Icons.Filled.Home
    ),

    BottomNavItem(
        title = "Search",
        route = "main_screen",
        selectedicon = Icons.Default.Search,
        unselectedicon = Icons.Filled.Search
    ),

    BottomNavItem(
        title = "Paylaşımlar",
        route="main_screen",
        selectedicon = Icons.Default.SendToMobile,
        unselectedicon = Icons.Filled.Send
    ),

    BottomNavItem(
        title = "Profil",
        route = "main_screen",
        selectedicon = Icons.Default.PersonPin,
        unselectedicon = Icons.Filled.PersonPin
    ),
    BottomNavItem(
        title = "Mesajlar",
        route = "main_screen",
        selectedicon = Icons.Default.Message,
        unselectedicon = Icons.Filled.Message
    )




)

