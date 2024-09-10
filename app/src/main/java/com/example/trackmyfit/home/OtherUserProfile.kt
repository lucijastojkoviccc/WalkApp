package com.example.trackmyfit.home
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.foundation.lazy.items


import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.trackmyfit.R
import com.example.trackmyfit.recorded.activity.Activity

@Composable
fun OtherUserProfileScreen(navController: NavController, userId: String) {
    var userData by remember { mutableStateOf<UserData?>(null) }
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    var activityIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current // Kontekst za pokretanje Intent-a

    // Fetch user data and activities
    LaunchedEffect(userId) {
        val db = FirebaseFirestore.getInstance()

        // Fetch user data
        val userDocument = db.collection("users").document(userId).get().await()
        userData = userDocument.toObject(UserData::class.java)

        Log.d("Activitiessss", userId)
        // Fetch activities for the user, sorted by date (assumed field name: timestamp)
        val activitiesSnapshot = db.collection("activities")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get().await()

        //activities = activitiesSnapshot.toObjects(Activity::class.java)

        // Kreiraj par (Activity, id) kako bi mogao da koristiš ID kad zatreba
        val activityList = activitiesSnapshot.documents.map { document ->
            document.toObject(Activity::class.java) to document.id
        }.filter { it.first != null } // Filtriraj null vrednosti

        activities = activityList.map { it.first!! }  // Preuzmi listu aktivnosti bez ID-jeva
        activityIds = activityList.map { it.second } // Lista ID-ova za aktivnosti

        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            userData?.let { user ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profilna slika
                    Image(
                        painter = rememberImagePainter(user.profilePictureUrl),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ime i prezime sa ikonicom za poruke
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = {
                            // Kreiraj Intent za slanje email-a
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:") // Samo email aplikacije će se otvarati
                                putExtra(
                                    Intent.EXTRA_EMAIL,
                                    arrayOf(user.email)
                                ) // Postavi email adresu
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "Hi I noticed you on TrackMyFit"
                                ) // Opcionalno: postavi naslov
                            }
                            context.startActivity(emailIntent) // Pokreni Intent
                        }) {
                            Icon(Icons.Default.Message, contentDescription = "Message Icon")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "${user.gender}",
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Visina i težina
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Visina: ${user.height} cm", fontSize = 16.sp)
                        Text(text = "Težina: ${user.weight} kg", fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Activities section
                    Text(
                        text = "Activities",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Lazy list for activities
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        itemsIndexed(activities) { index, activity -> // Koristi itemsIndexed da možeš dobiti index
                            val activityId = activityIds[index] // Pristupi ID-ju koristeći index

                            ActivityItem(
                                navController = navController,
                                onClick = {
                                    // Koristi ovaj lambda da uhvatiš ID
                                    navController.navigate("show_activity_screen/$activityId")
                                },
                                activityType = activity.type
                            )
                        }

                    }
                }
            }
        }

    }
}
@Composable
fun ActivityItem(navController: NavController, onClick: () -> Unit, activityType: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick), // Sada se onClick poziva kada se pritisne
        elevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            val icon = when (activityType) {
                "Running" -> R.drawable.ic_running
                "Cycling" -> R.drawable.ic_cycling
                "Rollerblading" -> R.drawable.ic_rollerblading
                else -> R.drawable.ic_default
            }

            Icon(
                painter = painterResource(id = icon),
                contentDescription = activityType,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = activityType,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 16.sp
            )
        }
    }
}






