package com.example.trackmyfit.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.firebase.firestore.Query
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

import java.util.*



@Composable
fun WalkingTabContent() {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var walksData by remember { mutableStateOf<Map<String, Int>>(emptyMap()) } // Map of date to total steps



    // Fetch data from Firestore
    LaunchedEffect(userId) {
        val db = FirebaseFirestore.getInstance()
        val walksSnapshot = db.collection("walks")
            .whereEqualTo("userId", userId)
            .orderBy("date")
            .get()
            .await()

        // Group walk data by date and sum the steps
        val groupedWalks = walksSnapshot.documents
            .groupBy { document ->
                document.getString("date") ?: "" // Group by date
            }.mapValues { entry ->
                entry.value.sumBy { document ->
                    document.getLong("steps")?.toInt() ?: 0 // Sum the steps
                }
            }.filterKeys { it.isNotEmpty() } // Remove empty dates if any

        walksData = groupedWalks.toSortedMap(compareByDescending { it }) // Sort by descending date
    }

    // Display the graph with scrollable bars
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(walksData.toList()) { (dateString, totalSteps) ->
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(dateString) ?: Date()
            WalkBar(totalSteps = totalSteps, date = date)
        }
    }
}

@Composable
fun WalkBar(totalSteps: Int, date: Date) {
    val maxSteps = 10000f // Define the max height for the bar based on a maximum step count
    val barHeightRatio = totalSteps / maxSteps

    // Format the date to day/month
    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
    val formattedDate = dateFormat.format(date)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // The Canvas will draw the bar with rounded corners
        Canvas(
            modifier = Modifier
                .height(150.dp) // Total height for the bars
                .width(15.dp)   // Width for the bars
        ) {
            val barHeight = size.height * barHeightRatio
            val cornerRadius = 8.dp.toPx() // Set the corner radius for the bars

            drawRoundRect(
                color = Color(0xFF0f7008),
                topLeft = Offset(0f, size.height - barHeight), // Start from the bottom
                size = Size(size.width, barHeight), // Dynamic height
                cornerRadius = CornerRadius(cornerRadius, cornerRadius) // Rounded corners
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = formattedDate,
            fontSize = 12.sp,
            color = Color(0xFF29CF1D) // Set date text color to lilac
        )
    }
}




