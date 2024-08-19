package com.example.trackmyfit

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trackmyfit.register.RegisterScreen
import com.example.trackmyfit.login.LoginScreen
import com.example.trackmyfit.home.MainScreen
import com.example.trackmyfit.home.EditProfileScreen
import com.example.trackmyfit.home.AddSpotScreen
import com.example.trackmyfit.home.ChatScreen
@Composable

fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = "register") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable("home") {
            MainScreen(navController = navController)
        }
        composable("editProfile") {
            EditProfileScreen(navController = navController)
        }

        composable("chat/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            ChatScreen(navController = navController, chatId = chatId!!)
        }
        composable("add_spot/{latitude}/{longitude}") { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull()
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull()
            AddSpotScreen(navController = navController, latitude = latitude, longitude = longitude)
        }



    }
}
