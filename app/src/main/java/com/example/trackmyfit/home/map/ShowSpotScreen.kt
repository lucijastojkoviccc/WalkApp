package com.example.trackmyfit.home.map
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowSpotScreen(navController: NavHostController, spotId: String, viewModel: SpotViewModel = viewModel()) {
    var workoutSpot by remember { mutableStateOf<WorkoutSpot?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedActivities by remember { mutableStateOf(listOf<String>()) }
    val allActivities = listOf("Running", "Cycling", "Rollerblade", "Hiking", "Gym", "Outdoor gym")

    LaunchedEffect(spotId) {
        viewModel.getWorkoutSpotById(spotId) { spot ->
            workoutSpot = spot
            selectedActivities = spot?.activities ?: emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Spot Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            workoutSpot?.let { spot ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Ime workout spota - ljubičasta boja i veći font
                    Text(
                        text = spot.name,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // "Activities" i plus ikona - veći font
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Activities",
                            style = MaterialTheme.typography.headlineSmall // Povećan font za "Activities"
                        )
                        IconButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Activity")
                        }
                    }

                    // Lista aktivnosti - veći font i Divider između stavki
                    LazyColumn {
                        items(spot.activities) { activity ->
                            Column {
                                Text(
                                    text = activity,
                                    style = MaterialTheme.typography.bodyLarge // Povećan font za aktivnosti
                                )
                                Divider(modifier = Modifier.padding(vertical = 8.dp)) // Divider između aktivnosti
                            }
                        }
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    )

    if (showDialog) {
        AddActivityDialog(
            currentActivities = workoutSpot?.activities ?: emptyList(),
            allActivities = allActivities,
            onDismiss = { showDialog = false },
            onSave = { newActivities ->
                val updatedActivities = workoutSpot?.activities?.toMutableList()?.apply {
                    addAll(newActivities)
                }
                if (updatedActivities != null) {
                    viewModel.updateWorkoutSpotActivities(spotId, updatedActivities) {
                        workoutSpot = workoutSpot?.copy(activities = updatedActivities)
                        showDialog = false
                    }
                }
            }
        )
    }
}
@Composable
fun AddActivityDialog(
    currentActivities: List<String>,
    allActivities: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var selectedNewActivities by remember { mutableStateOf(listOf<String>()) }

    // Filter out activities that are already present in the spot
    val availableActivities = allActivities.filter { it !in currentActivities }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Add Activity") },
        text = {
            LazyColumn {
                items(availableActivities) { activity ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedNewActivities.contains(activity),
                            onCheckedChange = { isChecked ->
                                selectedNewActivities = if (isChecked) {
                                    selectedNewActivities + activity
                                } else {
                                    selectedNewActivities - activity
                                }
                            }
                        )
                        Text(text = activity)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(selectedNewActivities) }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}