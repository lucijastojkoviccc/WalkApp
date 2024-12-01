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


class StepCounterViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount

    private var sensorManager: SensorManager? = null
    private var stepDetectorSensor: Sensor? = null
    private var stepCounterSensor: Sensor? = null
    private var isStepDetectorAvailable = false
    private var isStepCounterAvailable = false
    private var initialStepCount = -1 // Početna vrednost za Step Counter

    init {

        sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepDetectorSensor != null) {
            isStepDetectorAvailable = true
        } else {
            Log.e("Step", "Step Detector sensor not available!")
        }

        if (stepCounterSensor != null) {
            isStepCounterAvailable = true
        } else {
            Log.e("Step", "Step Counter sensor not available!")
        }
    }

    fun registerSensor() {
        stepDetectorSensor?.also { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        stepCounterSensor?.also { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun unregisterSensor() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (event.sensor.type) {
                Sensor.TYPE_STEP_DETECTOR -> {
                    _stepCount.value += 1
                }
                Sensor.TYPE_STEP_COUNTER -> {
                    if (initialStepCount == -1) {
                        initialStepCount = event.values[0].toInt()
                    }
                    _stepCount.value = event.values[0].toInt() - initialStepCount
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    fun resetStepCount() {
        // Reset Step Counter vrednosti tako što se osvežava početna vrednost
        initialStepCount = -1
        _stepCount.value = 0
    }

    fun calculateDistanceWalked(steps: Int, height: Float): Float {
        val strideLength = height * 0.414f // Dužina koraka u metrima
        val distanceInMeters = steps * strideLength
        return distanceInMeters / 1000f // U kilometrima
    }
}
