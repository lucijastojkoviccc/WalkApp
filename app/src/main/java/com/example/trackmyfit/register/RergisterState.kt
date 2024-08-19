package com.example.trackmyfit.register
import android.net.Uri

data class RegisterState( //user
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val height: String = "",
    val weight: String = "",
    val profilePictureUri: Uri? = null,
    val email: String = "",
    val error: String = "",
    val isRegistered: Boolean = false
)

