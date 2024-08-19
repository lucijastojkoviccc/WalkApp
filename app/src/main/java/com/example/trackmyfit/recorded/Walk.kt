package com.example.trackmyfit.recorded

data class Walk(
    val distance: Float = 0f,  // Distance in kilometers
    val calories: Int = 0,  // Calories burned
    val date: String = "",  // Date of the walk
    val userId: String = ""  // ID of the user
)
