package com.example.trackmyfit.recorded.walk
import androidx.core.content.ContextCompat
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.trackmyfit.service.StepCounterService

class StepCounterReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val serviceIntent = Intent(context, StepCounterService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
