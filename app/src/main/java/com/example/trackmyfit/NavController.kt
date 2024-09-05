package com.example.trackmyfit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.trackmyfit.register.RegisterScreen
import com.example.trackmyfit.login.LoginScreen
import com.example.trackmyfit.home.MainScreen
import com.example.trackmyfit.home.search.SearchScreen
//import com.example.trackmyfit.recorded.AddActivityScreen
import com.example.trackmyfit.home.UserProfileScreen
import com.example.trackmyfit.home.EditProfileScreen
import com.example.trackmyfit.home.map.AddSpotScreen

import com.example.trackmyfit.home.map.ShowSpotScreen
import com.example.trackmyfit.home.map.MapScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import android.util.Log
import com.example.trackmyfit.home.OtherUserProfileScreen
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
        composable(BottomNavItem.Search.route) {
            SearchScreen(navController = navController)
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


        composable("otherUserProfile/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            OtherUserProfileScreen(navController = navController, userId = userId)
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

