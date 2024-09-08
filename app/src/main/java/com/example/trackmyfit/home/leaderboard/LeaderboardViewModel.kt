package com.example.trackmyfit.home.leaderboard

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.trackmyfit.recorded.activity.Activity
import com.example.trackmyfit.home.search.User
import com.google.firebase.firestore.FirebaseFirestore

class LeaderboardViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    var leaderboardState by mutableStateOf<List<Pair<String, Double>>>(emptyList())

        private set

    fun fetchLeaderboard(activityType: String) {
        firestore.collection("activities")
            .whereEqualTo("type", activityType)
            .get()
            .addOnSuccessListener { result ->
                val userDistanceMap = mutableMapOf<String, Double>() // userId -> max distance

                for (document in result) {
                    val activity = document.toObject(Activity::class.java)
                    val userId = activity.userId
                    val distance = activity.distanceInKM
                    userDistanceMap[userId] = maxOf(userDistanceMap[userId] ?: 0.0, distance.toDouble())
                }

                // Convert the map to a sorted list of Pairs
                leaderboardState = userDistanceMap
                    .map { (userId, distance) -> userId to distance }
                    .sortedByDescending { it.second } // Sort by distance descending
            }
            .addOnFailureListener {
                leaderboardState = emptyList() // Handle failure case
            }
    }


//    private fun fetchUsers(userDistanceMap: Map<String, Double>) {
//        val users = mutableListOf<User>()
//        val userIds = userDistanceMap.keys.toList()
//
//        firestore.collection("users")
//            .whereIn("userId", userIds)
//            .get()
//            .addOnSuccessListener { result ->
//                for (document in result) {
//                    val user = document.toObject(User::class.java)
//                    users.add(user)
//                }
//
//                // Sort users by distance in descending order
//                leaderboardState = users.sortedByDescending { userDistanceMap[it.id] }
//            }
//            .addOnFailureListener {
//                leaderboardState = emptyList() // Handle failure case
//            }
//    }
}

