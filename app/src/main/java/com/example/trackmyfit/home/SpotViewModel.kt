package com.example.trackmyfit.home
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.location.Location

class SpotViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // LiveData ili StateFlow za praćenje trenutne lokacije
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> get() = _currentLocation

    // LiveData za praćenje spotova
    private val _workoutSpots = MutableStateFlow<List<WorkoutSpot>>(emptyList())
    val workoutSpots: StateFlow<List<WorkoutSpot>> get() = _workoutSpots

    init {

        loadWorkoutSpots()
    }


    fun saveWorkoutSpot(workoutSpot: WorkoutSpot, onSuccess: () -> Unit) {
        db.collection("workoutspot").add(workoutSpot)
            .addOnSuccessListener {
                // Spot uspešno dodat
                loadWorkoutSpots() // Učitaj ponovo sve workout spotove
                onSuccess() // Pozovi callback funkciju za navigaciju nazad
            }
            .addOnFailureListener { exception ->
                // Greška prilikom dodavanja
            }
    }

    fun loadWorkoutSpots() {
        db.collection("workoutspot").get()
            .addOnSuccessListener { snapshot ->
                val spots = snapshot.documents.mapNotNull { it.toObject(WorkoutSpot::class.java) }
                _workoutSpots.value = spots
            }
            .addOnFailureListener {
                // Greška prilikom dohvatanja spotova
            }
    }

    fun refreshMap() {
        loadWorkoutSpots()
    }
}

