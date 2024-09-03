package com.example.trackmyfit.recorded.walk
import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.Calendar
import java.util.concurrent.TimeUnit
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log
import kotlinx.coroutines.flow.StateFlow

//class StepCounterViewModel : ViewModel() {
//    private val _stepCount = MutableStateFlow(0)
//    val stepCount = _stepCount.asStateFlow()
//
//
//    private var stepsAtMidnight: Int = 0
//    private lateinit var sensorManager: SensorManager
//    private lateinit var stepCounterSensor: Sensor
//
//
//    private val sensorEventListener = object : SensorEventListener {
//        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//        override fun onSensorChanged(event: SensorEvent?) {
//            event?.let {
//                val totalSteps = event.values[0].toInt()
//                // Ažuriraj broj koraka u realnom vremenu
//                _stepCount.value = totalSteps - stepsAtMidnight
//            }
//        }
//    }
//
//    fun updateStepCount(totalSteps: Int) {
//        _stepCount.value = totalSteps
//    }
//    fun startStepCounting(context: Context) {
//        sensorManager = ContextCompat.getSystemService(context, SensorManager::class.java)!!
//
//        // Inicijalizacija senzora za brojanje koraka
//        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!!
//
//        stepCounterSensor?.let { sensor ->
//            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI)
//        } ?: run {
//            Log.e("StepCounter", "Step Counter Sensor is not available on this device.")
//        }
//
//        // Učitavanje koraka u ponoć iz SharedPreferences
//        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
//        stepsAtMidnight = sharedPreferences.getInt("stepsAtMidnight", 0)
//    }
//
//    fun calculateCaloriesBurned(steps: Int, weight: Float, height: Float, age: Int, gender: String): Int {
//        // Formula for calories burned calculation
//        return (steps * 0.04 * weight).toInt() // Ovo je primer jednostavne formule
//    }
//
//    fun calculateDistanceWalked(steps: Int, height: Float, gender: String): Float {
//        // Formula for calculating distance based on steps and height
//        val strideLength = when (gender.lowercase()) {
//            "male" -> height * 0.415f
//            "female" -> height * 0.413f
//            else -> height * 0.414f
//        }
//
//        // Calculate distance in meters, then convert to kilometers
//        val distanceInMeters = steps * strideLength
//        return distanceInMeters / 1000f
//    }
//    fun resetStepsAtMidnight(context: Context) {
//        // Resetuj korake
//        val sharedPreferences = context.getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE)
//        val stepsAtMidnight = sharedPreferences.getInt("stepsAtMidnight", 0)
//        _stepCount.value = 0
//
//        // Sačuvaj resetovane korake
//        sharedPreferences.edit().putInt("stepsAtMidnight", _stepCount.value).apply()
//    }
//
//    fun scheduleMidnightReset(context: Context) {
//        val calendar = Calendar.getInstance().apply {
//            set(Calendar.HOUR_OF_DAY, 0)
//            set(Calendar.MINUTE, 0)
//            set(Calendar.SECOND, 0)
//        }
//
//        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()
//        val workRequest = PeriodicWorkRequestBuilder<ResetStepsWorker>(1, TimeUnit.DAYS)
//            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
//            .build()
//
//        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
//            "reset_steps_at_midnight",
//            ExistingPeriodicWorkPolicy.REPLACE,
//            workRequest
//        )
//    }
//
//}
class StepCounterViewModel : ViewModel() {
    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    // Funkcija za ažuriranje broja koraka
    fun updateStepCount(totalSteps: Int) {
        _stepCount.value = totalSteps
    }

    // Funkcija za proračun kalorija na osnovu koraka
//    fun calculateCaloriesBurned(steps: Int, weight: Float): Int {
//        return (steps * 0.04 * weight).toInt()
//    }

    // Funkcija za proračun distance na osnovu koraka i visine
//    fun calculateDistanceWalked(steps: Int, height: Float): Float {
//        val strideLength = height * 0.415f // Proračun dužine koraka u metrima
//        val distanceInMeters = steps * strideLength
//        return distanceInMeters / 1000f // Pređeno u kilometrima
//    }
    fun calculateDistanceWalked(steps: Int, height: Float, gender: String): Float {
        // Estimate stride length based on height and gender
        val strideLength = when (gender.lowercase()) {
            "male" -> height * 0.415f // Stride length for men
            "female" -> height * 0.413f // Stride length for women
            else -> height * 0.414f // Default if gender is unknown
        }

        // Calculate distance in meters
        val distanceInMeters = steps * strideLength

        // Convert distance to kilometers
        return distanceInMeters / 1000f
    }

    fun calculateCaloriesBurned(steps: Int, weight: Float, height: Float, age: Int, gender: String): Int {
        // Harris-Benedict BMR calculation
        val bmr = when (gender.lowercase()) {
            "male" -> 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
            "female" -> 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
            else -> throw IllegalArgumentException("Invalid gender. Must be 'male' or 'female'")
        }

        // Activity factor (light activity as default)
        val activityFactor = 1.375

        // Total Daily Energy Expenditure (TDEE)
        val tdee = bmr * activityFactor

        // Average number of steps per day (defaulting to 10,000 steps)
        val averageStepsPerDay = 10000

        // Calculate calories burned per step
        val caloriesPerStep = tdee / averageStepsPerDay

        // Calculate total calories burned for the given number of steps
        return (steps * (caloriesPerStep / weight)).toInt()
    }

}
