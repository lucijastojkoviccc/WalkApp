package com.example.trackmyfit.recorded.walk
//import android.content.Context
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.Date
//
//class ResetStepsWorker(
//    context: Context,
//    workerParams: WorkerParameters
//) : CoroutineWorker(context, workerParams) {
//
//    override suspend fun doWork(): Result {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.failure()
//        val db = FirebaseFirestore.getInstance()
//        val sharedPreferences = applicationContext.getSharedPreferences("StepCounterPrefs", Context.MODE_PRIVATE)
//        val totalSteps = sharedPreferences.getInt("totalSteps", 0)
//
//        // Pre nego što resetujemo korake, sačuvamo podatke u Firestore
//        val distance = sharedPreferences.getFloat("distanceWalked", 0f)
//        val calories = sharedPreferences.getInt("caloriesBurned", 0)
//        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
//
//        val walk = Walk(
//            steps = totalSteps,
//            distance = distance,
//            calories = calories,
//            date = date,
//            userId = userId
//        )
//
//        db.collection("walks").add(walk)
//
//        // Resetuj korake
//        with(sharedPreferences.edit()) {
//            putInt("totalSteps", 0)
//            apply()
//        }
//
//        return Result.success()
//    }
//}
