package com.example.trackmyfit.home
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.material.AlertDialog
@Composable
fun FallDetectionSystem(
    onFallDetected: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    // Sensor event listener
    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    // Calculate magnitude of acceleration
                    val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                    // Threshold for detecting a fall
                    if (magnitude > 30) { // Adjust this value based on testing
                        onFallDetected()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No need to handle this for now
            }
        }
    }

    // Register/unregister sensor listener based on lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                sensorManager.unregisterListener(sensorEventListener)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            sensorManager.unregisterListener(sensorEventListener)
        }
    }
}
@Composable
fun ShowFallDialog(
    onYesClick: () -> Unit,
    onNoClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Prevent dismissing by clicking outside */ },
        title = {
            Text(text = "Are you okay?", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { onYesClick() },
                    modifier = Modifier.weight(1f).padding(end = 4.dp)
                ) {
                    Text("Yes")
                }
                Button(
                    onClick = { onNoClick() },
                    modifier = Modifier.weight(1f).padding(start = 4.dp)
                ) {
                    Text("No")
                }
            }
        }
    )
}

