package com.example.trackmyfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.trackmyfit.ui.theme.TrackMyFitTheme
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.*
import com.example.trackmyfit.register.RegisterScreen


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackMyFitTheme {
                // Check if the user is registered or not
                val isUserRegistered = remember { mutableStateOf(false) }

                // Here, implement logic to check if the user is registered
                // This can be done by checking if the user data exists in Firebase or in local storage
                // For simplicity, let's assume it's hardcoded for now:
                isUserRegistered.value = checkIfUserIsRegistered()

                if (isUserRegistered.value) {
                    // Show the main screen (e.g., HomeScreen) if the user is registered
                    //HomeScreen()
                } else {
                    // Show the RegisterScreen if the user is not registered
                    RegisterScreen()
                }

            }
        }
    }
    private fun checkIfUserIsRegistered(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser != null
    }
}
