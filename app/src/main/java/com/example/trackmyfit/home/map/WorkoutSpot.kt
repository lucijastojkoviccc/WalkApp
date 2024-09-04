package com.example.trackmyfit.home.map
data class WorkoutSpot(
    var id: String = "",
    val name: String = "",
    val activities: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val userId: String = ""
)
