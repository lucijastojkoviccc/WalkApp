package com.example.trackmyfit.recorded
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*



class SleepViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    fun saveSleepSession(startMillis: Long, endMillis: Long, length: String, userId: String) {
        val sleepData = hashMapOf(
            "start" to startMillis,
            "end" to endMillis,
            "length" to length,
            "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(endMillis)),
            "userId" to userId
        )

        db.collection("sleep").add(sleepData)
            .addOnSuccessListener {
                Log.d("SleepViewModel", "Sleep session recorded successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("SleepViewModel", "Failed to save sleep session", exception)
            }
    }
}
