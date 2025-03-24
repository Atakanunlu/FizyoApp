package com.example.fizyoapp.presentation.ui.bottomnavbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.SendToMobile
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Search


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
        route = "user_main_screen",
        selectedicon = Icons.Default.Home,
        unselectedicon = Icons.Filled.Home
    ),

    BottomNavItem(
        title = "Search",
        route = "user_main_screen",
        selectedicon = Icons.Default.Search,
        unselectedicon = Icons.Filled.Search
    ),

    BottomNavItem(
        title = "Paylaşımlar",
        route="user_main_screen",
        selectedicon = Icons.AutoMirrored.Filled.SendToMobile,
        unselectedicon = Icons.AutoMirrored.Filled.Send
    ),

    BottomNavItem(
        title = "Profil",
        route = "user_main_screen",
        selectedicon = Icons.Default.PersonPin,
        unselectedicon = Icons.Filled.PersonPin
    ),
    BottomNavItem(
        title = "Mesajlar",
        route = "user_main_screen",
        selectedicon = Icons.AutoMirrored.Filled.Message,
        unselectedicon = Icons.AutoMirrored.Filled.Message
    )




)

