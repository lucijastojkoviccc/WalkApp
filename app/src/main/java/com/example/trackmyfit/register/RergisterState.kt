package com.example.trackmyfit.register
import android.net.Uri

data class RegisterState( //user
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val height: Number = 0,
    val weight: Number = 0,
    val profilePictureUri: Uri? = null,
    val email: String = "",
    val isRegistered: Boolean = false,
    val error: String = ""
)

