package com.example.trackmyfit.recorded
import com.example.trackmyfit.recorded.StepCounterViewModel
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.trackmyfit.recorded.WalkSaver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MidnightResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        runBlocking {
            val viewModel = StepCounterViewModel()
            viewModel.resetStepsAtMidnight(context) // Reset steps if the app is not active
        }
    }
}
