package com.example.trackmyfit.recorded

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.util.Log

class StepCounterViewModel : ViewModel() {
    private val _stepCount = MutableStateFlow(0)
    val stepCount = _stepCount.asStateFlow()

    private var lastDaySteps = 0
    private var lastDayCalories = 0
    private var lastDayDistance = 0f

    private lateinit var sensorManager: SensorManager
    private lateinit var stepCounterSensor: Sensor
    private var stepsAtMidnight: Int = 0

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val totalSteps = event.values[0].toInt()

                // Calculate today's steps
                _stepCount.value = totalSteps - stepsAtMidnight
            }
        }
    }

    fun startStepCounting(context: Context) {
        sensorManager = ContextCompat.getSystemService(context, SensorManager::class.java)!!

        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!!

        stepCounterSensor?.let { sensor ->
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI)
        } ?: run {
            Log.e("StepCounter", "Step Counter Sensor is not available on this device.")
        }

        // Load the stored stepsAtMidnight from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        stepsAtMidnight = sharedPreferences.getInt("stepsAtMidnight", 0)
    }

    fun resetStepsAtMidnight(context: Context) {
        // Store the current step count at midnight
        val sharedPreferences = context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        stepsAtMidnight = _stepCount.value
        sharedPreferences.edit().putInt("stepsAtMidnight", stepsAtMidnight).apply()

        // Save last day's steps, calories, and distance for database storage
        lastDaySteps = _stepCount.value
        lastDayCalories = calculateCaloriesBurned(lastDaySteps) // Implement your calculation logic here
        lastDayDistance = calculateDistanceWalked(lastDaySteps) // Implement your calculation logic here
    }

    fun getLastDaySteps(): Int = lastDaySteps
    fun getLastDayCalories(): Int = lastDayCalories
    fun getLastDayDistance(): Float = lastDayDistance

    private fun calculateCaloriesBurned(steps: Int): Int {
        // Add your logic for calculating calories burned
        return 0
    }

    private fun calculateDistanceWalked(steps: Int): Float {
        // Add your logic for calculating distance walked
        return 0f
    }
}

