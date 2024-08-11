package com.example.trackmyfit.register
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.net.Uri
class RegisterViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> get() = _state

    private val firestore = FirebaseFirestore.getInstance()

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

    fun onHeightChange(newValue: String) {
        _state.value = _state.value.copy(height = newValue)
    }

    fun onWeightChange(newValue: String) {
        _state.value = _state.value.copy(weight = newValue)
    }

    fun onProfilePictureChange(uri: Uri?) {
        _state.value = _state.value.copy(profilePictureUri = uri)
    }
    fun onRegisterClick() {
        val user = mapOf(
            "firstName" to state.value.firstName,
            "lastName" to state.value.lastName,
            "birthDate" to state.value.birthDate,
            "email" to state.value.email,
            "gender" to state.value.gender,
            "height" to state.value.height,
            "weight" to state.value.weight,
            "profilePictureUri" to state.value.profilePictureUri?.toString()
        )

        firestore.collection("users")
            .add(user)
            .addOnSuccessListener {
                // Uspešno sačuvano, možete dodati logiku za navigaciju
            }
            .addOnFailureListener {
                // Desila se greška
            }
    }
}
