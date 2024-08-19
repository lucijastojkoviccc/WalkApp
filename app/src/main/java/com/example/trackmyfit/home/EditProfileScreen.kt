package com.example.trackmyfit.home

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@Composable
fun EditProfileScreen(navController: NavController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    var userData by remember { mutableStateOf(UserData()) }
    var isLoading by remember { mutableStateOf(true) }
    var newProfilePictureUri by remember { mutableStateOf<Uri?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val showImagePickerDialog = remember { mutableStateOf(false) }

    // Launchers for picking images from the gallery or taking a photo
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            newProfilePictureUri = uri
        }
    }
    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            newProfilePictureUri = saveBitmapToUri(context, bitmap, userData.email)
        }
    }

    LaunchedEffect(userId) {
        userId?.let {
            val db = FirebaseFirestore.getInstance()
            val userDocument = db.collection("users").document(it).get().await()
            userData = userDocument.toObject(UserData::class.java) ?: UserData()
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        if (showImagePickerDialog.value) {
            AlertDialog(
                onDismissRequest = { showImagePickerDialog.value = false },
                title = { Text("Choose Profile Picture") },
                text = {
                    Column {
                        IconButton(onClick = {
                            galleryLauncher.launch("image/*")
                            showImagePickerDialog.value = false
                        }) {
                            Icon(imageVector = Icons.Default.Photo, contentDescription = "Gallery")
                            Text("Choose from Gallery")
                        }
                        IconButton(onClick = {
                            cameraLauncher.launch(null)
                            showImagePickerDialog.value = false
                        }) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera")
                            Text("Take a Photo")
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showImagePickerDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profilna slika
            Image(
                painter = rememberImagePainter(newProfilePictureUri ?: userData.profilePictureUrl),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable {
                        showImagePickerDialog.value = true
                    },
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Ime i prezime
            OutlinedTextField(
                value = userData.firstName,
                onValueChange = { userData = userData.copy(firstName = it) },
                label = { Text("First Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userData.lastName,
                onValueChange = { userData = userData.copy(lastName = it) },
                label = { Text("Last Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Visina i težina
            OutlinedTextField(
                value = userData.height.toString(),
                onValueChange = { userData = userData.copy(height = it.toIntOrNull() ?: userData.height) },
                label = { Text("Height (cm)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userData.weight.toString(),
                onValueChange = { userData = userData.copy(weight = it.toIntOrNull() ?: userData.weight) },
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Pol
            OutlinedTextField(
                value = userData.gender ?: "",
                onValueChange = { userData = userData.copy(gender = it) },
                label = { Text("Gender") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save dugme
            Button(
                onClick = {
                    coroutineScope.launch {
                        val db = FirebaseFirestore.getInstance()
                        val userRef = db.collection("users").document(userId!!)
                        val storageRef = FirebaseStorage.getInstance().reference

                        // Delete the previous profile picture if it exists
                        userData.profilePictureUrl?.let { url ->
                            if (url.isNotEmpty()) {
                                val previousProfilePicRef = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                                previousProfilePicRef.delete().await()
                            }
                        }

                        // Upload the new profile picture if available
                        newProfilePictureUri?.let {
                            val fileName = "${userData.email}_profpic.png"
                            val profilePicRef = storageRef.child("profile_pictures/$userId/$fileName")
                            profilePicRef.putFile(it).await()
                            val downloadUrl = profilePicRef.downloadUrl.await().toString()
                            userData = userData.copy(profilePictureUrl = downloadUrl)
                        }

                        // Save updated user data
                        userRef.set(userData).await()
                        navController.previousBackStackEntry?.savedStateHandle?.set("needRefresh", true) // Refresh UserProfileScreen
                        navController.navigateUp()  // Return to UserProfileScreen
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(text = "Save")
            }
        }
    }
}

// Function to save a bitmap and return a Uri
fun saveBitmapToUri(context: Context, bitmap: Bitmap, userEmail: String): Uri? {
    val fileName = "${userEmail}_profpic.png"
    val file = File(context.getExternalFilesDir(null), fileName)

    return try {
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        Uri.fromFile(file)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


