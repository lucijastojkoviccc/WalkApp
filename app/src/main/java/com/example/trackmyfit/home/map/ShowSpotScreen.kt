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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable

import androidx.compose.ui.text.input.TextFieldValue
import com.example.trackmyfit.BottomNavItem
import com.example.trackmyfit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowSpotScreen(
    navController: NavHostController,
    spotId: String,
    viewModel: SpotViewModel = viewModel()
) {
    var workoutSpot by remember { mutableStateOf<WorkoutSpot?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedActivities by remember { mutableStateOf(listOf<String>()) }
    val allActivities = listOf("Running", "Cycling", "Rollerblade", "Hiking", "Gym", "Outdoor gym")
    var showCommentDialog by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }
    val comments by viewModel.comments.collectAsState(initial = emptyList())

    LaunchedEffect(spotId) {
        viewModel.getWorkoutSpotById(spotId) { spot ->
            workoutSpot = spot
            selectedActivities = spot?.activities ?: emptyList()
        }
        viewModel.getCommentsForSpot(spotId) // Fetch comments for this spot
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
                    // Ime workout spota
                    Text(
                        text = spot.name,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // "Activities" i plus ikona
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Activities",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Activity")
                        }
                    }

                    // Lista aktivnosti
                    LazyColumn {
                        items(spot.activities) { activity ->
                            Column {
                                Text(
                                    text = activity,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // "Comments" deo
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Comments",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        IconButton(onClick = { showCommentDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Comment")
                        }
                    }

                    // Lista komentara, sortirana po najnovijim na vrhu
                    LazyColumn {
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                navController = navController  // Prosleđivanje navController-a za navigaciju
                            )
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
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

    if (showCommentDialog) {
        AddCommentDialog(
            commentText = commentText,
            onDismiss = { showCommentDialog = false },
            onSave = { newComment ->
                viewModel.addComment(spotId, newComment) {
                    showCommentDialog = false
                }
            }
        )
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    navController: NavHostController,  // Dodajemo NavHostController za navigaciju
    viewModel: SpotViewModel = viewModel()
) {
    var user by remember { mutableStateOf<UserComm?>(null) }

    // Pokrećemo povlačenje podataka o korisniku na osnovu userId
    LaunchedEffect(comment.userId) {
        viewModel.getUserData(comment.userId) { fetchedUser ->
            user = fetchedUser
        }
    }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid

// Koristimo Modifier.clickable da omogućimo klikanje na CommentItem
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (comment.userId == currentUserId) {
                    // Ako je komentar trenutno ulogovanog korisnika, idi na njegov profil
                    navController.navigate(BottomNavItem.Profile.route)
                } else {
                    // Ako je komentar od drugog korisnika, idi na otherUserProfile
                    navController.navigate("otherUserProfile/${comment.userId}")
                }
            }
            .padding(vertical = 8.dp)
    ) {
        // Prikaz profilne slike
        val profileImageUrl = user?.profilePictureUrl
        Image(
            painter = if (profileImageUrl != null) {
                rememberImagePainter(data = profileImageUrl)
            } else {
                painterResource(id = R.drawable.person) // Placeholder za default sliku
            },
            contentDescription = "Profile Picture",
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Prikazivanje imena ili "You" za trenutnog korisnika
        val currentUser = FirebaseAuth.getInstance().currentUser
        Text(
            text = if (currentUser?.uid == comment.userId) "You" else "${user?.firstName ?: ""} ${user?.lastName ?: ""}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.weight(1f))

        // Datum i vreme komentara
        comment.timestamp?.let { timestamp ->
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = (timestamp as? com.google.firebase.Timestamp)?.toDate()
            Text(text = sdf.format(date ?: Date()))
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Prikazivanje samog komentara ispod imena
    Text(
        text = comment.text,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(start = 48.dp) // Pomeri tekst komentara ispod slike
    )
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
@Composable
fun AddCommentDialog(
    commentText: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newComment by remember { mutableStateOf(TextFieldValue(commentText)) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Add Comment") },
        text = {
            Column {
                Text("Enter your comment below:")
                BasicTextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(newComment.text) }) {
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