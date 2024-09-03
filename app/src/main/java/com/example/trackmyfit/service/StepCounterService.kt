package com.example.trackmyfit.service
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.trackmyfit.R
import com.example.trackmyfit.recorded.walk.StepCounterViewModel
//class StepCounterService : Service() {
//
//    private lateinit var stepCounterViewModel: StepCounterViewModel
//
//    override fun onCreate() {
//        super.onCreate()
//        stepCounterViewModel = StepCounterViewModel()  // Kreiraj ViewModel ili ga nabavi na drugi način
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        startBackgroundStepCounting(this)
//        return START_STICKY
//    }
//
//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
//
//    private fun startBackgroundStepCounting(context: Context) {
//        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
//
//        stepCounterSensor?.let { sensor ->
//            sensorManager.registerListener(object : SensorEventListener {
//                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//                override fun onSensorChanged(event: SensorEvent?) {
//                    event?.let {
//                        val totalSteps = event.values[0].toInt()
//                        stepCounterViewModel.updateStepCount(totalSteps)  // Ažuriraj broj koraka u ViewModel
//                    }
//                }
//            }, sensor, SensorManager.SENSOR_DELAY_NORMAL)
//        }
//    }
//}
class StepCounterService : Service() {

    private lateinit var sensorManager: SensorManager
    private lateinit var stepCounterSensor: Sensor
    private var initialStepCount = -1

    private val stepListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val totalSteps = event.values[0].toInt()

                // Koraci od početka aplikacije
                if (initialStepCount == -1) {
                    initialStepCount = totalSteps
                }

                val currentStepCount = totalSteps - initialStepCount
                Log.d("StepCounterService", "Steps detected: $currentStepCount")

                // TODO: Ažuriraj korake u ViewModel-u ili ih sačuvaj u SharedPreferences za kasnije
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        startStepCounting()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        val channelId = "step_counter_channel"
        val channelName = "Step Counter Service"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(notificationChannel)

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Step Counter")
            .setContentText("Counting your steps...")
            //.setSmallIcon(R.drawable.ic_steps)
            .build()

        startForeground(1, notification)
    }

    private fun startStepCounting() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!!

        if (stepCounterSensor != null) {
            sensorManager.registerListener(stepListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            Log.e("StepCounterService", "Step Counter Sensor not available")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(stepListener)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

