package com.example.trackmyfit


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {

    object Search : BottomNavItem(
        route = "search",
        icon = Icons.Default.Search,
        title = "Search"
    )
    object Leaderboard : BottomNavItem(
        route = "lead",
        icon = Icons.Default.List,
        title = "LB"
    )

    object Map : BottomNavItem(
        route = "map",
        icon = Icons.Default.Map,
        title = "Map"
    )
    object Add : BottomNavItem(
        route = "add",
        icon = Icons.Default.Add,
        title = "Add"
    )
    object Profile : BottomNavItem(
        route = "profile",
        icon = Icons.Default.Person,
        title = "Profile"
    )
}
