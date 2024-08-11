package com.example.trackmyfit.register
import android.net.Uri

data class RegisterState(
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: String = "",
    val email: String = "",
    val gender: String = "",
    val height: String = "",
    val weight: String = "",
    val profilePictureUri: Uri? = null
)

