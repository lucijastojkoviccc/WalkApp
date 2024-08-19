package com.example.trackmyfit.register

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import android.app.DatePickerDialog
import android.widget.DatePicker

import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.material.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.Color

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showImageDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let {
                imageBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                viewModel.onProfilePictureChange(it)
            }
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            imageBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            viewModel.onProfilePictureChange(uri)
        }
    }

    if (showImageDialog) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Surface(shape = MaterialTheme.shapes.medium, elevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Image", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        showImageDialog = false
                        val tempFile = File.createTempFile("profile", ".jpg", context.cacheDir).apply {
                            createNewFile()
                            deleteOnExit()
                        }
                        imageUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            tempFile
                        )
                        imageUri?.let {
                            cameraLauncher.launch(it)
                        }
                    }) {
                        Text("Take a Picture")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        showImageDialog = false
                        galleryLauncher.launch("image/*")
                    }) {
                        Text("Choose from Gallery")
                    }
                }
            }
        }
    }

    // Local state for the selected date
    var selectedDate by remember { mutableStateOf("") }

    // DatePicker dialog
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            selectedDate = "$dayOfMonth/${month + 1}/$year"
            viewModel.onBirthDateChange(selectedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Register", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.firstName,
            onValueChange = { viewModel.onFirstNameChange(it) },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = state.lastName,
            onValueChange = { viewModel.onLastNameChange(it) },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // BirthDate Input with Calendar Icon
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.birthDate,
                onValueChange = { viewModel.onBirthDateChange(it) },
                label = { Text("Date of Birth") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Select Date")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Gender Radio Buttons
        Text(text = "Gender", style = MaterialTheme.typography.h6)
        Row {
            RadioButton(
                selected = state.gender == "male",
                onClick = { viewModel.onGenderChange("male") }
            )
            Text(text = "Male", modifier = Modifier.padding(end = 16.dp))

            RadioButton(
                selected = state.gender == "female",
                onClick = { viewModel.onGenderChange("female") }
            )
            Text(text = "Female")
        }

        Spacer(modifier = Modifier.height(16.dp))


//        OutlinedTextField(
//            value = state.gender,
//            onValueChange = { viewModel.onGenderChange(it) },
//            label = { Text("Gender") },
//            modifier = Modifier.fillMaxWidth()
//        )

        OutlinedTextField(
            value = state.height,
            onValueChange = { viewModel.onHeightChange(it) },
            label = { Text("Height") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            trailingIcon = { Text("cm") }
        )

        OutlinedTextField(
            value = state.weight,
            onValueChange = { viewModel.onWeightChange(it) },
            label = { Text("Weight") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            trailingIcon = { Text("kg") }
        )
        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            //keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Display profile picture or camera icon
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap!!.asImageBitmap(),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            IconButton(onClick = { showImageDialog = true }) {
                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Select Image")
            }
        }

        // Ostali elementi interfejsa za unos korisničkih podataka (npr. ime, email...)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { viewModel.register(email, password) }) {
            Text(text = "Register")
        }

        if (state.error.isNotEmpty()) {
            Text(text = state.error, color = MaterialTheme.colors.error)
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Dodajemo Log In tekst
        Text(
            text = "Log In",
            color = Color.Blue,
            modifier = Modifier
                .clickable { navController.navigate("login") }
                .padding(vertical = 8.dp),
            textDecoration = TextDecoration.Underline
        )
    }
    if (state.isRegistered) {
        LaunchedEffect(Unit) {
            navController.navigate("home") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

}
