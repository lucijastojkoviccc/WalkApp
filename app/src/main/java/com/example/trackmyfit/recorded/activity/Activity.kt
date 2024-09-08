package com.example.trackmyfit.recorded.activity
import android.net.Uri

data class Activity(
    var distanceInKM: Float=0f,
    var timeInMillis: Long=0L,
    var caloriesBurned: Int=0,
    var type: String="",
    var userId: String="",
    var timestamp: Long = System.currentTimeMillis()
)
