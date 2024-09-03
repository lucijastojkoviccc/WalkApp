package com.example.trackmyfit.home.map
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpotViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // LiveData ili StateFlow za praćenje trenutne lokacije
//    private val _currentLocation = MutableStateFlow<Location?>(null)
//    val currentLocation: StateFlow<Location?> get() = _currentLocation

    // LiveData za praćenje spotova
    private val _workoutSpots = MutableStateFlow<List<WorkoutSpot>>(emptyList())
    val workoutSpots: StateFlow<List<WorkoutSpot>> get() = _workoutSpots

    init {

        loadWorkoutSpots()
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
}

