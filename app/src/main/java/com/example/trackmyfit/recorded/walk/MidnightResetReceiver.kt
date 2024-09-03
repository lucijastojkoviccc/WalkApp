package com.example.trackmyfit.recorded.walk
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MidnightResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Ovde dodaj logiku za resetovanje koraka
        val viewModel = StepCounterViewModel()  // Ili na neki drugi način osiguraj pristup ViewModel-u
        //viewModel.resetStepsAtMidnight(context!!)
    }
}