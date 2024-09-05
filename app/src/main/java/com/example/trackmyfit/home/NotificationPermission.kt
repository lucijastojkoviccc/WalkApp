package com.example.trackmyfit.home
import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current

    // Request notification permission if Android version is 13 or higher
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (!isGranted) {
                    // Handle case when permission is not granted
                    Toast.makeText(context, "Notification permission is required", Toast.LENGTH_LONG).show()
                }
            }
        )

        LaunchedEffect(Unit) {
            notificationPermissionState.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

