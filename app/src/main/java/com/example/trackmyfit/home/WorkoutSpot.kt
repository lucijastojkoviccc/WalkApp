package com.example.trackmyfit.home
data class WorkoutSpot(
    val name: String = "",
    val activities: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val userId: String = ""
)
