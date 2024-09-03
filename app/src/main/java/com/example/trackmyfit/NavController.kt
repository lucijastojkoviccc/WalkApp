package com.example.trackmyfit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.trackmyfit.register.RegisterScreen
import com.example.trackmyfit.login.LoginScreen
import com.example.trackmyfit.home.MainScreen
import com.example.trackmyfit.home.chat.ChatListScreen
//import com.example.trackmyfit.recorded.AddActivityScreen
import com.example.trackmyfit.home.UserProfileScreen
import com.example.trackmyfit.home.EditProfileScreen
import com.example.trackmyfit.home.map.AddSpotScreen
import com.example.trackmyfit.home.chat.ChatScreen
import com.example.trackmyfit.home.map.ShowSpotScreen
import com.example.trackmyfit.home.map.MapScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.util.Log
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("home") {
            MainScreen(navController = navController)
        }
        composable(BottomNavItem.Chat.route) {
            ChatListScreen(navController = navController)
        }
        composable(BottomNavItem.Add.route) {
            //AddActivityScreen(navController)
        }
        composable("map"/*BottomNavItem.Map.route*/) {
            MapScreen(navController = navController)
        }
        composable(BottomNavItem.Profile.route) {
            UserProfileScreen(navController = navController)
        }
        composable("editProfile") {
            EditProfileScreen(navController = navController)
        }
        composable(
            route = "chat/{clickedUserId}",
            arguments = listOf(navArgument("clickedUserId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clickedUserId = backStackEntry.arguments?.getString("clickedUserId") ?: ""
            Log.d("NavigationAA", "Navigating to ChatScreen with userId: $clickedUserId")
            ChatScreen(navController = navController, clickedUserId = clickedUserId)
            Log.d("NavigationAA", "Finished navigation to ChatScreen")
        }



        composable("show_spot/{spotId}") { backStackEntry ->
            val spotId = backStackEntry.arguments?.getString("spotId")
            if (spotId != null) {
                ShowSpotScreen(navController = navController, spotId = spotId)
            }
        }
        composable(
            route = "add_spot/{latitude}/{longitude}",
            arguments = listOf(
                navArgument("latitude") { type = NavType.StringType },
                navArgument("longitude") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()

            if (latitude != null && longitude != null) {
                AddSpotScreen(
                    navController = navController,  // Dodaj ovaj parametar
                    latitude = latitude,
                    longitude = longitude
                )
            }
        }

    }
}

