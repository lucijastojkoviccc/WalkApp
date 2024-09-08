package com.example.trackmyfit
//import com.example.trackmyfit.recorded.walk.MidnightResetReceiver
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.trackmyfit.ui.theme.TrackMyFitTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import com.google.firebase.FirebaseApp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        //createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1002)
            }
        }

        setContent {
            TrackMyFitTheme {

                val navController = rememberNavController()
                CheckUserLoggedIn(navController)
            }
        }

            }


        @RequiresApi(Build.VERSION_CODES.O)
        @Composable
        fun CheckUserLoggedIn(navController: NavHostController) {
            val auth = remember { FirebaseAuth.getInstance() }

            // Odredi početnu destinaciju na osnovu toga da li je korisnik prijavljen
            val startDestination = if (auth.currentUser != null) "home" else "login"

            // Prikazivanje navigacionog hosta sa odgovarajućom početnom destinacijom
            AppNavHost(navController = navController, startDestination = startDestination)
        }
}




