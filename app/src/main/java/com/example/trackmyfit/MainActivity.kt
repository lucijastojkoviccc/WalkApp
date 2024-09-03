package com.example.trackmyfit
import com.example.trackmyfit.recorded.walk.MidnightResetReceiver
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
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import com.example.trackmyfit.service.StepCounterService
import androidx.core.content.ContextCompat

import java.util.Calendar
import android.provider.Settings
import com.google.firebase.FirebaseApp
import com.example.trackmyfit.recorded.walk.StepCounterReceiver

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        createNotificationChannel(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1002)
            }
        }
        //scheduleMidnightReset(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (!alarmManager.canScheduleExactAlarms()) {
                // Ako aplikacija nema dozvolu, pokreni zahtev
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            } else {

                scheduleMidnightReset(this) //novije verzije andr
            }
        } else {
            // Za starije verzije Androida, možeš direktno zakazati alarm
            scheduleMidnightReset(this)
        }
        setContent {
            TrackMyFitTheme {

                val navController = rememberNavController()
                CheckUserLoggedIn(navController)
            }
        }
    // Registruj BroadcastReceiver za BOOT_COMPLETED događaj
    val intentFilter = IntentFilter(Intent.ACTION_BOOT_COMPLETED)
    val receiver = StepCounterReceiver()
    registerReceiver(receiver, intentFilter)
    val serviceIntent = Intent(this, StepCounterService::class.java)
    ContextCompat.startForegroundService(this, serviceIntent)
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
//    private fun checkIfUserIsRegistered(): Boolean {
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        return currentUser != null
//    }
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
//@Composable
//fun CheckUserLoggedIn(navController: NavHostController) {
//    val auth = remember { FirebaseAuth.getInstance() }
//
//    // Odredi početnu destinaciju na osnovu toga da li je korisnik prijavljen
//    val startDestination = if (auth.currentUser != null) "home" else "login"
//
//    // Prikazivanje navigacionog hosta sa odgovarajućom početnom destinacijom
//    AppNavHost(navController = navController, startDestination = startDestination)
//}


//@Composable
//fun CheckUserLoggedIn(navController: NavHostController) {
//    val auth = remember { FirebaseAuth.getInstance() }
//
//    // Log za proveru trenutnog korisnika
//    val currentUser = auth.currentUser
//    Log.d("AuthCheck", "Current user: $currentUser")
//
//    LaunchedEffect(Unit) {
//        // Odlaganje provere za slučaj da FirebaseAuth još nije ažuriran
//        kotlinx.coroutines.delay(500)
//
//        val updatedUser = auth.currentUser
//        Log.d("AuthCheck", "Updated user after delay: $updatedUser")
//
//        if (updatedUser != null) {
//            Log.d("AuthCheck", "Navigating to home")
//            navController.navigate("home") {
//                popUpTo("login") { inclusive = true }
//            }
//        } else {
//            Log.d("AuthCheck", "Navigating to login")
//            navController.navigate("login") {
//                popUpTo("home") { inclusive = true }
//            }
//        }
//    }
//}
@Composable
fun CheckUserLoggedIn(navController: NavHostController) {
    val auth = remember { FirebaseAuth.getInstance() }

    // Odredi početnu destinaciju na osnovu toga da li je korisnik prijavljen
    val startDestination = if (auth.currentUser != null) "home" else "login"

    // Prikazivanje navigacionog hosta sa odgovarajućom početnom destinacijom
    AppNavHost(navController = navController, startDestination = startDestination)
}



