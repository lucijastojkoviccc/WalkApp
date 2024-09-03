package com.example.trackmyfit.register
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
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
import androidx.activity.ComponentActivity
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.unit.dp

import androidx.compose.ui.text.input.KeyboardCapitalization
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
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager

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
import com.google.android.material.datepicker.MaterialDatePicker
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import java.text.SimpleDateFormat
import java.util.* // Za Date i Locale
import java.util.Locale
import android.Manifest
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = viewModel()) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    var height by remember { mutableStateOf(state.height.toFloat()) }
    var weight by remember { mutableStateOf(state.weight.toFloat()) }

    var showImageDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

//    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
//        if (success) {
//            imageUri?.let {
//                imageUri = it
//                val correctedBitmap = correctImageRotation(context, it)
//                imageBitmap = correctedBitmap
//                viewModel.onProfilePictureChange(it)
//            }
//        }
//    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let { uri ->
                try {
                    // Dodajemo proveru i ispravljanje rotacije slike slikane kamerom
                    val correctedBitmap = correctImageRotation(context, uri)
                    if (correctedBitmap != null) {
                        imageBitmap = correctedBitmap
                        viewModel.onProfilePictureChange(uri)
                        showImageDialog = false
                    } else {
                        // Ako ispravljanje rotacije nije uspelo, koristimo originalni bitmap
                        imageBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                        viewModel.onProfilePictureChange(uri)
                    }
                } catch (e: Exception) {
                    Log.e("CameraImage", "Failed to correct image rotation", e)
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Ako je dozvola odobrena, pokreni kameru
                val tempFile = File.createTempFile("profile", ".jpg", context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)).apply {
                    createNewFile()
                    deleteOnExit()
                }
                imageUri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".fileprovider",
                    tempFile
                )
                imageUri?.let { uri ->
                    cameraLauncher.launch(uri)
                }
            } else {
                // Dozvola nije odobrena, obavesti korisnika
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    )


    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            val correctedBitmap = correctImageRotation(context, it)
            imageBitmap = correctedBitmap
            viewModel.onProfilePictureChange(it)
        }
    }

    if (showImageDialog) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Surface(shape = RoundedCornerShape(8.dp), elevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Select Image", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        // Proveri da li je dozvola za kameru odobrena, ako nije, pokreni launcher za dozvolu
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            // Ako već ima dozvolu, direktno pokreni kameru
                            val tempFile = File.createTempFile("profile", ".jpg", context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)).apply {
                                createNewFile()
                                deleteOnExit()
                            }
                            imageUri = FileProvider.getUriForFile(
                                context,
                                context.packageName + ".fileprovider",
                                tempFile
                            )
                            imageUri?.let { uri ->
                                cameraLauncher.launch(uri)
                            }
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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Register", style = MaterialTheme.typography.h4)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = state.firstName,
            onValueChange = { viewModel.onFirstNameChange(it) },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Words
            )
        )

        OutlinedTextField(
            value = state.lastName,
            onValueChange = { viewModel.onLastNameChange(it) },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Words
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically // Ovim poravnavaš vertikalno unutar reda
        ) {
            OutlinedTextField(
                value = selectedDate,
                onValueChange = { viewModel.onBirthDateChange(it) },
                label = { Text("Date of Birth") },
                modifier = Modifier.weight(1f), // Ovo će omogućiti OutlinedTextField-u da zauzme što više prostora
                readOnly = true
            )
            Spacer(modifier = Modifier.width(8.dp)) // Razmak između TextField-a i ikone
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Select Date")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))



        // Gender Radio Buttons
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Gender", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp)) // Dodaj malo prostora između teksta i radio dugmadi
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = state.gender == "male",
                        onClick = { viewModel.onGenderChange("male") }
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Razmak između radio dugmeta i teksta
                    Text(text = "Male")
                }
                Spacer(modifier = Modifier.width(32.dp)) // Razmak između dve grupe radio dugmeta i teksta
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = state.gender == "female",
                        onClick = { viewModel.onGenderChange("female") }
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // Razmak između radio dugmeta i teksta
                    Text(text = "Female")
                }
            }
        }


        Spacer(modifier = Modifier.height(10.dp))



        Text(text = "Height: ${height.toInt()} cm")
        Slider(
            value = height,
            onValueChange = { newHeight ->
                height = newHeight
                viewModel.onHeightChange(newHeight.toInt())
            },
            valueRange = 100f..250f, // Primer opsega za visinu
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Weight: ${weight.toInt()} kg")
        Slider(
            value = weight,
            onValueChange = { newWeight ->
                weight = newWeight
                viewModel.onWeightChange(newWeight.toInt())
            },
            valueRange = 30f..200f, // Primer opsega za težinu
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { newemail->
                email = newemail
                viewModel.onEmailChange(newemail)
                },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(10.dp))

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
        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            if (state.email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.register(email, password)
            } else {
                Toast.makeText(context, "Prazno!", Toast.LENGTH_SHORT).show()
            }

        }) {
            Text(text = "Register")
        }

        if (state.error.isNotEmpty()) {
            Text(text = state.error, color = MaterialTheme.colors.error)
        }


        Spacer(modifier = Modifier.height(10.dp))
        // Dodajemo Log In tekst
        Text(
            text = "Log In",
            color = Color.Blue,
            modifier = Modifier
                .clickable { navController.navigate("login") }
                .padding(vertical = 8.dp),
            textDecoration = TextDecoration.Underline
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
    if (state.isRegistered) {
        LaunchedEffect(Unit) {
            navController.navigate("home") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

}
//fun correctImageRotation(context: Context, uri: Uri): Bitmap? {
//    val inputStream = context.contentResolver.openInputStream(uri)
//    val bitmap = BitmapFactory.decodeStream(inputStream)
//    val exif = ExifInterface(inputStream!!)
//    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
//    inputStream.close()
//
//    val matrix = Matrix()
//    when (orientation) {
//        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
//        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
//        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
//    }
//    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
//}
fun correctImageRotation(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        // Ponovo otvaramo InputStream za EXIF podatke
        val exifStream = context.contentResolver.openInputStream(uri)
        val exif = ExifInterface(exifStream!!)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        exifStream.close()

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } catch (e: Exception) {
        Log.e("CorrectImageRotation", "Failed to correct image rotation", e)
        null
    }
}



