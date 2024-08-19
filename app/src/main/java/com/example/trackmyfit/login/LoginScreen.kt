package com.example.trackmyfit.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext



@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Login", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            //keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password)

        )
        //visualTransformation = PasswordVisualTransformation()
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.login(email, password) }) {
            Text(text = "Log In")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Forgot your password?",
            color = MaterialTheme.colors.primary,
            modifier = Modifier.clickable {
                // Poziva funkciju za resetovanje šifre

                sendPasswordResetEmail(context, email)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Navigate to Register Screen
        Text(
            text = "Don't have an account? Sign in",
            color = Color.Blue,
            modifier = Modifier.clickable { navController.navigate("register") },
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (loginState is LoginState.Error) {
            Text(
                text = (loginState as LoginState.Error).error,
                color = Color.Red
            )
        }
    }

    // Handle login success
    if (loginState is LoginState.Success) {
        LaunchedEffect(Unit) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }
}
fun sendPasswordResetEmail(context: Context, email: String) {
    if (email.isNotEmpty()) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Reset password email sent.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    } else {
        Toast.makeText(context, "Please enter your email address.", Toast.LENGTH_LONG).show()
    }
}