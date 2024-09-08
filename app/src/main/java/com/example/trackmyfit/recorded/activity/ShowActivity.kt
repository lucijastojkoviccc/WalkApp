package com.example.trackmyfit.recorded.activity

import com.google.firebase.storage.FirebaseStorage
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.Text
import androidx.compose.runtime.Composable


@Composable
fun ShowActivityScreen(navController: NavController, activityId: String) {
    var activityData by remember { mutableStateOf<Activity?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // Firebase instance
    val db = FirebaseFirestore.getInstance()
    val storageReference = FirebaseStorage.getInstance().reference

    // Učitavanje podataka o aktivnosti iz Firestore-a
    LaunchedEffect(activityId) {
        val document = db.collection("activities").document(activityId).get().await()
        activityData = document.toObject(Activity::class.java)

        // Učitavanje slike iz Firebase Storage-a
        val imageRef = storageReference.child("snapshots/$activityId.jpg")
        imageRef.downloadUrl.addOnSuccessListener { uri ->
            imageUrl = uri.toString()
        }

        isLoading = false
    }

    // Prikaz učitavanja dok čekamo podatke
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    } else {
        activityData?.let { activity ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Prikaz slike sa Firebase Storage-a
                imageUrl?.let {
                    Image(
                        painter = rememberImagePainter(it),
                        contentDescription = "Activity Image",
                        modifier = Modifier
                            .height(250.dp)
                            .fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Prikaz podataka o aktivnosti
                Text(text = "Time: ${formatTime(activity.timeInMillis)}", fontWeight = FontWeight.Medium, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Distance: ${activity.distanceInKM} km", fontWeight = FontWeight.Medium, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Calories: ${activity.caloriesBurned}", fontWeight = FontWeight.Medium, fontSize = 20.sp)
            }
        }
    }
}

// Formatira vreme u obliku hh:mm:ss
fun formatTime(timeInMillis: Long): String {
    val hours = (timeInMillis / (1000 * 60 * 60)) % 24
    val minutes = (timeInMillis / (1000 * 60)) % 60
    val seconds = (timeInMillis / 1000) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
