package com.example.trackmyfit
import com.example.trackmyfit.recorded.MidnightResetReceiver
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.trackmyfit.ui.theme.TrackMyFitTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import android.app.AlarmManager
import android.app.PendingIntent

import android.content.Intent
import java.util.Calendar


class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted")
            } else {
                Log.d("MainActivity", "Notification permission denied")
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1002)
            }
        }
        scheduleMidnightReset(this)
        setContent {
            TrackMyFitTheme {
                // Inicijalizacija navController-a
                val navController = rememberNavController()

                // Provera da li je korisnik već ulogovan i postavljanje odgovarajućeg ekrana
                CheckUserLoggedIn(navController)
            }
        }
    }
    // Metoda za rukovanje rezultatima zahteva za dozvolu

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Message Channel"
            val descriptionText = "Channel for chat message notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("chat_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun checkIfUserIsRegistered(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser != null
    }
    private fun scheduleMidnightReset(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MidnightResetReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}

@Composable
fun CheckUserLoggedIn(navController: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }

    LaunchedEffect(key1 = auth) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Ako je korisnik već ulogovan, preusmeriti na home screen
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        } else {
            // Ako korisnik nije ulogovan, preusmeriti na login screen
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    // Pokretanje glavne navigacije aplikacije
    AppNavHost(navController = navController)
}
