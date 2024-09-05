package com.example.trackmyfit.home.map
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.*
import java.text.SimpleDateFormat
import java.util.*
class SpotViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // LiveData ili StateFlow za praćenje trenutne lokacije
//    private val _currentLocation = MutableStateFlow<Location?>(null)
//    val currentLocation: StateFlow<Location?> get() = _currentLocation

    // LiveData za praćenje spotova
    private val _workoutSpots = MutableStateFlow<List<WorkoutSpot>>(emptyList())
    val workoutSpots: StateFlow<List<WorkoutSpot>> get() = _workoutSpots

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments
    init {

        loadWorkoutSpots()
    }
    fun getCommentsForSpot(spotId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("comments")
            .whereEqualTo("spotId", spotId)
            .orderBy("timestamp", Query.Direction.DESCENDING)  // Sort comments by newest first
            .addSnapshotListener { value, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                val commentsList = value?.documents?.mapNotNull { it.toObject(Comment::class.java) }
                _comments.value = commentsList ?: emptyList()
            }
    }
//    fun addComment(spotId: String, commentText: String, onComplete: () -> Unit) {
//        val db = FirebaseFirestore.getInstance()
//        val currentUser = FirebaseAuth.getInstance().currentUser
//        val timestamp = FieldValue.serverTimestamp() // Timestamp from Firestore server
//
//        val comment = Comment(
//            userId = currentUser?.uid ?: "",
//            userFirstName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Unknown",
//            userLastName = currentUser?.displayName?.split(" ")?.lastOrNull() ?: "Unknown",
//            spotId = spotId,
//            text = commentText,
//            timestamp = timestamp // Save the current timestamp
//        )
//
//        db.collection("comments")
//            .add(comment)
//            .addOnSuccessListener {
//                onComplete() // Close dialog on success
//            }
//            .addOnFailureListener {
//                // Handle error
//            }
//    }
//fun addComment(spotId: String, commentText: String, onComplete: () -> Unit) {
//    val db = FirebaseFirestore.getInstance()
//    val currentUser = FirebaseAuth.getInstance().currentUser
//    val timestamp = FieldValue.serverTimestamp() // Firebase timestamp
//
//    val comment = Comment(
//        userId = currentUser?.uid ?: "",
//        userFirstName = currentUser?.displayName?.split(" ")?.firstOrNull() ?: "Unknown",
//        userLastName = currentUser?.displayName?.split(" ")?.lastOrNull() ?: "Unknown",
//        spotId = spotId,
//        text = commentText,
//        timestamp = timestamp
//    )
//
//    db.collection("comments")
//        .add(comment)
//        .addOnSuccessListener {
//            // Fetch comments again to reflect the new comment immediately
//            getCommentsForSpot(spotId)
//            onComplete()
//        }
//        .addOnFailureListener {
//            // Handle failure
//        }
//}
fun addComment(spotId: String, commentText: String, onComplete: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val timestamp = FieldValue.serverTimestamp()

    val comment = Comment(
        userId = currentUser?.uid ?: "",
        spotId = spotId,
        text = commentText,
        timestamp = timestamp
    )

    db.collection("comments")
        .add(comment)
        .addOnSuccessListener {
            getCommentsForSpot(spotId)
            onComplete()
        }
        .addOnFailureListener {
            // Handle failure
        }
}



    fun getWorkoutSpotById(spotId: String, onResult: (WorkoutSpot?) -> Unit) {
        db.collection("workoutspots").document(spotId).get()
            .addOnSuccessListener { document ->
                val spot = document.toObject(WorkoutSpot::class.java)
                onResult(spot)
            }
            .addOnFailureListener {
                onResult(null) // Handle failure case
            }
    }

    fun saveWorkoutSpot(workoutSpot: WorkoutSpot, onSuccess: (String) -> Unit) {
        // Add workout spot and let Firestore generate the ID
        db.collection("workoutspots").add(workoutSpot)
            .addOnSuccessListener { documentReference ->
                // Get the generated ID
                val generatedId = documentReference.id

                // You can now use this ID, e.g., save it to the spot object if necessary or pass it in a callback
                loadWorkoutSpots() // Reload all spots
                onSuccess(generatedId) // Callback with the generated ID
            }
            .addOnFailureListener { exception ->
                // Handle failure
            }
    }

    fun loadWorkoutSpots() {
        db.collection("workoutspots").get()
            .addOnSuccessListener { snapshot ->
                val spots = snapshot.documents.mapNotNull { document ->
                    document.toObject(WorkoutSpot::class.java)?.copy(id = document.id) // Assign Firestore document ID
                }
                _workoutSpots.value = spots
            }
            .addOnFailureListener {
                // Handle failure
            }
    }
    fun refreshMap() {
        loadWorkoutSpots()
    }
    fun updateWorkoutSpotActivities(spotId: String, updatedActivities: List<String>, onComplete: () -> Unit) {
        db.collection("workoutspots").document(spotId)
            .update("activities", updatedActivities)
            .addOnSuccessListener {
                onComplete()
            }
            .addOnFailureListener {
                // Handle failure case
            }
    }
    fun getUserData(userId: String, onComplete: (UserComm?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(UserComm::class.java)
                    onComplete(user)
                } else {
                    onComplete(null) // Ako korisnik ne postoji
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

}
//data class Comment(
//    val userId: String = "",
//    val userFirstName: String = "",
//    val userLastName: String = "",
//    val spotId: String = "",
//    val text: String = "",
//    val timestamp: Any? = null // Firestore timestamp
//)
data class Comment(
    val userId: String = "",
    val spotId: String = "",
    val text: String = "",
    val timestamp: Any? = null // Firestore timestamp
)
data class UserComm(
    val firstName: String = "",
    val lastName: String = "",
    val profilePictureUrl: String? = null
)