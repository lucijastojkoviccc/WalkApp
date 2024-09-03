package com.example.trackmyfit.recorded
import android.graphics.Bitmap
import com.google.firebase.Timestamp

data class Activity(
    var img: Bitmap? =null,
    var timestamp: Long=0L,
    var avgSpeedInKHM: Float=0f,
    var distanceInKM: Float=0f,
    var timeInMillis: Long=0L,
    var caloriesBurned: Int=0
)
{

}
