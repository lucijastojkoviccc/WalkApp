package com.example.trackmyfit.recorded.activity

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.firebase.firestore.FirebaseFirestore
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Period


class StopwatchViewModel : ViewModel() {

    // Timer state
    private var stopwatchJob: Job? = null
    var stopwatchTime by mutableStateOf("00:00:00")
        private set
    var isRunning by mutableStateOf(false)
        private set

    var secondsElapsed = 0L
    private set
    // Live location tracking components
    private var fusedLocationClient: FusedLocationProviderClient? = null
    var pathPoints = mutableStateListOf<LatLng>()
    private set

    fun startStopwatch(fusedLocationClient: FusedLocationProviderClient) {
        isRunning = true
        this.fusedLocationClient = fusedLocationClient
        pathPoints.clear()

        // Start tracking location
        startLocationUpdates()

        // Start the stopwatch
        stopwatchJob = viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                secondsElapsed++
                stopwatchTime = formatTime(secondsElapsed)
            }
        }
    }
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // Update every 5 seconds
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult ?: return
            for (location in locationResult.locations) {
                val latLng = LatLng(location.latitude, location.longitude)
                pathPoints.add(latLng)
            }
        }
    }

    fun finishTracking() {
        stopStopwatch()
        // Perform additional cleanup or saving data if needed
    }

    fun stopStopwatch() {
        stopwatchJob?.cancel()
        isRunning = false
        stopLocationUpdates()
    }


    fun cancelStopwatch() {
        stopwatchJob?.cancel()
        secondsElapsed = 0
        stopwatchTime = "00:00:00"
        pathPoints.clear()  // Clear the path
        isRunning = false
        stopLocationUpdates()
    }

    fun calculateDistance(): Float {
        var totalDistance = 0f
        if (pathPoints.size > 1) {
            for (i in 0 until pathPoints.size - 1) {
                val startPoint = pathPoints[i]
                val endPoint = pathPoints[i + 1]
                val results = FloatArray(1)
                Location.distanceBetween(
                    startPoint.latitude, startPoint.longitude,
                    endPoint.latitude, endPoint.longitude,
                    results
                )
                totalDistance += results[0]
            }
        }
        return totalDistance / 1000 // Convert to kilometers
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveActivityToDatabase(activityType: String, onActivitySaved: (String) -> Unit) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val user = FirebaseAuth.getInstance().currentUser
            val userId = user?.uid ?: return@launch


            val activity = Activity(
                //img = null,  // Sada sačuvajte Uri slike
                distanceInKM = calculateDistance(),
                timeInMillis = secondsElapsed * 1000,  // Pretvaranje u milisekunde
                caloriesBurned = burnedCalories(secondsElapsed, activityType),
                type = activityType,
                userId = userId,
                timestamp = System.currentTimeMillis()
            )

            val docRef = db.collection("activities").add(activity).await()
            val activityId = docRef.id
            onActivitySaved(activityId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun burnedCalories(secondsElapsed: Long, activityType: String): Int {
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return 0

        // Dohvatanje korisničkih podataka
        val userDoc = db.collection("users").document(userId).get().await()
        val weight = userDoc.getDouble("weight") ?: 0.0
        val gender = userDoc.getString("gender") ?: "Unknown"
        //val birthDate = userDoc.getString("birthDate") ?: ""

        // Izračunavanje godina
        //val age = calculateAge(birthDate)

        // Ukupna distanca pređena u kilometrima
        val distanceInKM = calculateDistance()

        // Konverzija vremena u minute
        val minutesElapsed = secondsElapsed / 60.0

        // Bazalna potrošnja kalorija po aktivnosti
        val caloriesPerMinutePerKg = when (activityType) {
            "Running" -> 0.075 // Kalorije po kilogramu po minuti za trčanje
            "Cycling" -> 0.05  // Kalorije po kilogramu po minuti za biciklizam
            "Rollerblading" -> 0.065 // Kalorije po kilogramu po minuti za rolanje
            else -> 0.0
        }

        // Izračunavanje ukupnih sagorelih kalorija
        val caloriesBurned = (caloriesPerMinutePerKg * weight * minutesElapsed).toInt()

        return caloriesBurned
    }

    fun updateActivityWithImage(activityId: String, imgUri: Uri) {
        val db = FirebaseFirestore.getInstance()
        db.collection("activities").document(activityId)
            .update("img", imgUri.toString())  // Ažuriramo dokument sa URI-jem slike
            .addOnSuccessListener {
                Log.d("StopwatchViewModel", "Activity image updated successfully!")
            }
            .addOnFailureListener { e ->
                Log.w("StopwatchViewModel", "Error updating activity image", e)
            }
    }



    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }
}
