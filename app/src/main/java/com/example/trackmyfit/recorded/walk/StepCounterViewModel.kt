package com.example.trackmyfit.recorded.walk
import android.app.Application
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
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow


class StepCounterViewModel(application: Application) : AndroidViewModel(application), SensorEventListener{
    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount


    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var isStepSensorAvailable = false

    init {
        sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (stepSensor != null) {
            isStepSensorAvailable = true
        } else {
            Log.e("StepCounterViewModel", "Step sensor not available!")
        }
    }

    // Register the sensor listener
    fun registerSensor() {
        stepSensor?.also { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    // Unregister the sensor listener
    fun unregisterSensor() {
        sensorManager?.unregisterListener(this)
    }

    // Reset step count
    fun resetStepCount() {
        _stepCount.value = 0
    }

    // Listen to sensor changes
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                // Increment step count manually on each step detection
                _stepCount.value += 1
                Log.d("StepCounterViewModel", "Step detected! Total: ${_stepCount.value}")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

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
