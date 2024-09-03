package com.example.trackmyfit.register

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.util.*
import kotlinx.coroutines.launch
class RegisterViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> get() = _state

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    fun onFirstNameChange(newValue: String) {
        _state.value = _state.value.copy(firstName = newValue)
    }

    fun onLastNameChange(newValue: String) {
        _state.value = _state.value.copy(lastName = newValue)
    }

    fun onBirthDateChange(newValue: String) {
        _state.value = _state.value.copy(birthDate = newValue)
    }

    fun onEmailChange(newValue: String) {
        _state.value = _state.value.copy(email = newValue)
    }

    fun onGenderChange(newValue: String) {
        _state.value = _state.value.copy(gender = newValue)
    }

    fun onHeightChange(newValue: Number) {
        _state.value = _state.value.copy(height = newValue)
    }

    fun onWeightChange(newValue: Number) {
        _state.value = _state.value.copy(weight = newValue)
    }

    fun onProfilePictureChange(uri: Uri?) {
        _state.value = _state.value.copy(profilePictureUri = uri)
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let { currentUser ->
                                // Sačuvaj profilnu sliku ako postoji
                                _state.value.profilePictureUri?.let { uri ->
                                    val storageRef = storage.reference.child("profile_pictures/${currentUser.uid}.jpg")
                                    storageRef.putFile(uri)
                                        .addOnSuccessListener { taskSnapshot ->
                                            taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                                                saveUserDataToFirestore(currentUser.uid, downloadUri.toString())
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            _state.value = _state.value.copy(error = e.message ?: "An error occurred")
                                        }
                                } ?: run {
                                    // Ako profilna slika ne postoji, sačuvaj podatke bez slike
                                    saveUserDataToFirestore(currentUser.uid, null)
                                }
                            }
                        } else {
                            _state.value = _state.value.copy(error = "Registration failed")
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "An error occurred")
            }
        }
    }

    private fun saveUserDataToFirestore(userId: String, profileImageUrl: String?) {
        // Obezbedi da se podaci za email i birthDate pravilno čuvaju
        val userData = mapOf(
            "firstName" to _state.value.firstName,
            "lastName" to _state.value.lastName,
            "birthDate" to _state.value.birthDate, // Dodaj birthDate
            "gender" to _state.value.gender,
            "height" to _state.value.height,
            "weight" to _state.value.weight,
            "email" to _state.value.email, // Dodaj email
            "profilePictureUrl" to profileImageUrl // Može biti null
        )

        firestore.collection("users").document(userId).set(userData)
            .addOnSuccessListener {
                _state.value = _state.value.copy(isRegistered = true)
            }
            .addOnFailureListener { e ->
                _state.value = _state.value.copy(error = e.message ?: "An error occurred")
            }
    }

}
