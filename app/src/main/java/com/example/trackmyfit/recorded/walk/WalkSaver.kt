package com.example.trackmyfit.recorded.walk

//import android.content.Context
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import android.util.Log
//import kotlinx.coroutines.tasks.await
//import java.text.SimpleDateFormat
//import java.util.*
//
//object WalkSaver {
//    suspend fun saveWalkToDatabase(
//        context: Context,
//        steps: Int,
//        calories: Int,
//        distance: Float
//    ) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        val date = getCurrentDate()
//
//        val walk = Walk(
//            distance = distance,
//            calories = calories,
//            date = date,
//            userId = userId
//        )
//
//        try {
//            val db = FirebaseFirestore.getInstance()
//            db.collection("walks").add(walk).await()
//            Log.d("WalkSaver", "Walk data saved successfully")
//        } catch (e: Exception) {
//            Log.e("WalkSaver", "Error saving walk data", e)
//        }
//    }
//
//    private fun getCurrentDate(): String {
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        return dateFormat.format(Date())
//    }
//}
