package com.example.trackmyfit.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSpotScreen(navController: NavHostController, latitude: Double?, longitude: Double?, viewModel: SpotViewModel = viewModel()) {
    var spotName by remember { mutableStateOf("") }
    val activityOptions = listOf("Run", "Cycling", "Rollerblade", "Hiking", "Gym", "Outdoor gym")
    val selectedActivities = remember { mutableStateListOf<String>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Workout Spot") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                TextField(
                    value = spotName,
                    onValueChange = { spotName = it },
                    label = { Text("Spot Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lazy list for checkboxes
                LazyColumn {
                    items(activityOptions) { activity ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedActivities.contains(activity),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        selectedActivities.add(activity)
                                    } else {
                                        selectedActivities.remove(activity)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = activity)
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Save and Cancel buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text("CANCEL")
                    }

                    Button(
                        onClick = {
                            if (spotName.isNotBlank() && latitude != null && longitude != null) {
                                val newSpot = WorkoutSpot(
                                    name = spotName,
                                    activities = selectedActivities.toList(),
                                    latitude = latitude,
                                    longitude = longitude,
                                    userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                )

                                // Save spot and navigate back to map
                                viewModel.saveWorkoutSpot(newSpot) {
                                    // Vraćamo se nazad na mapu i osvežavamo mapu
                                    navController.popBackStack() // Vraća se na MapScreen
                                    viewModel.refreshMap()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text("SAVE")
                    }
                }
            }
        }
    )
}
