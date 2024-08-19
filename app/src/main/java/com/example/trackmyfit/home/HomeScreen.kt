package com.example.trackmyfit.home
import com.example.trackmyfit.recorded.SleepViewModel
import com.example.trackmyfit.recorded.StepCounterViewModel
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale
import com.example.trackmyfit.recorded.WalkSaver
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import com.google.android.gms.fitness.data.SleepStages
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.request.DataReadRequest
import java.util.concurrent.TimeUnit

@Composable
fun MainScreen(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Chat,
        BottomNavItem.Add,
        BottomNavItem.Map,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { item ->
                    BottomNavigationItem(
                        icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                        label = { Text(text = item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                navController.graph.startDestinationRoute?.let { route ->
                                    popUpTo(route) {
                                        saveState = true
                                    }
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home", // Ovo je sada HomeScreen
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreenContent() // Dodajte sadržaj vašeg HomeScreen-a ovde
            }
            composable(BottomNavItem.Chat.route) {
                ChatListScreen(navController)
            }
            composable(BottomNavItem.Add.route) {
                //AddActivityScreen()
            }
            composable(BottomNavItem.Map.route) {
                MapScreen(navController)
            }
            composable(BottomNavItem.Profile.route) {
                UserProfileScreen(navController)
            }
        }
    }
}

@Composable
fun HomeScreenContent(viewModel: StepCounterViewModel = viewModel(), sleepViewModel: SleepViewModel = viewModel()) {
    val context = LocalContext.current
    val stepCount by viewModel.stepCount.collectAsState()
    val scope = rememberCoroutineScope()

    // State za čuvanje podataka o korisniku
    var weight by remember { mutableStateOf(70f) }
    var height by remember { mutableStateOf(170) }
    var heightinm by remember { mutableStateOf(1.7f)    }
    var gender by remember { mutableStateOf("male") }
    var birthday by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(0) }

    // State za spavanje
    var sleepStart by remember { mutableStateOf<Long?>(null) }
    var sleepEnd by remember { mutableStateOf<Long?>(null) }
    var sleepLength by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.startStepCounting(context)

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                weight = document.getDouble("weight")?.toFloat() ?: 70f
                height = document.getDouble("height")?.toInt() ?:170
                gender = document.getString("gender") ?: "male"
                birthday = document.getString("birthday") ?: ""

                heightinm=height/100f
                if (birthday.isNotEmpty()) {
                    age = calculateAge(birthday)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeScreenContent", "Error getting user data", exception)
            }
        // Fetch sleep data from Firestore
        scope.launch {
            try {
                val sleepCollection = db.collection("sleep")
                val querySnapshot = sleepCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("start", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val sleepDocument = querySnapshot.documents[0]
                    sleepStart = sleepDocument.getLong("start")
                    sleepEnd = sleepDocument.getLong("end")
                    sleepLength = sleepDocument.getString("length") ?: ""
                }
            } catch (e: Exception) {
                Log.e("HomeScreenContent", "Error fetching sleep data", e)
            }
        }

    }
    //var heightinm = height/100f
    val caloriesBurned = calculateCaloriesBurned(stepCount, weight, heightinm, age, gender)
    val distanceWalked = calculateDistanceWalked(stepCount, heightinm, gender)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Krug
        Canvas(modifier = Modifier.size(200.dp)) {
            drawCircle(
                color = Color(0xFFD7BDE2), // Svetlo ljubičasta boja
                style = Stroke(width = 20f)
            )
        }

        // Broj koraka
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$stepCount steps",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${calculateCaloriesBurned(stepCount, weight, heightinm, age, gender)} kcal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${calculateDistanceWalked(stepCount, heightinm, gender)} km",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Prikaz spavanja
            Text(
                text = "Sleeping",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Canvas(modifier = Modifier.size(150.dp)) {
                drawCircle(
                    color = Color.Black,
                    style = Stroke(width = 20f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (sleepLength.isNotEmpty()) sleepLength else "0h 0min",
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFD7BDE2)
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Buttons for Good Night and Good Morning
            Row {
                // Good Night button
                Button(
                    onClick = {
                        sleepStart = System.currentTimeMillis() // Start timer
                        sleepEnd = null // Reset end time
                        sleepLength = "" // Reset length
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
                ) {
                    Text(text = "Good night!", color = Color(0xFFD7BDE2))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Good Morning button
                Button(
                    onClick = {
                        sleepEnd = System.currentTimeMillis()
                        if (sleepStart != null && sleepEnd != null) {
                            val sleepDuration = sleepEnd!! - sleepStart!!
                            val hours = TimeUnit.MILLISECONDS.toHours(sleepDuration)
                            val minutes = TimeUnit.MILLISECONDS.toMinutes(sleepDuration) % 60
                            sleepLength = "${hours}h ${minutes}min"

                            // Save sleep session to Firestore
                            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                            sleepViewModel.saveSleepSession(sleepStart!!, sleepEnd!!, sleepLength, userId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD7BDE2))
                ) {
                    Text(text = "Good Morning", color = Color.Black)
                }
            }
        }
    }
    LaunchedEffect(stepCount) {
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.HOUR_OF_DAY) == 0 && calendar.get(Calendar.MINUTE) == 0) {
            WalkSaver.saveWalkToDatabase(context, stepCount, caloriesBurned, distanceWalked)
            viewModel.resetStepsAtMidnight(context) // Reset steps at midnight
        }
    }
}
fun calculateDistanceWalked(steps: Int, height: Float, gender: String): Float {
    // Estimate stride length based on height and gender
    val strideLength = when (gender.lowercase()) {
        "male" -> height * 0.415f // Stride length for men
        "female" -> height * 0.413f // Stride length for women
        else -> height * 0.414f // Default if gender is unknown
    }

    // Calculate distance in meters
    val distanceInMeters = steps * strideLength

    // Convert distance to kilometers
    return distanceInMeters / 1000f
}
fun calculateAge(birthday: String): Int {
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val birthDate = dateFormat.parse(birthday)
    val today = Calendar.getInstance()

    val birthCalendar = Calendar.getInstance().apply {
        time = birthDate
    }

    var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

    if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
        age--
    }

    return age
}

fun calculateCaloriesBurned(steps: Int, weight: Float, height: Float, age: Int, gender: String): Int {
    // Harris-Benedict BMR calculation
    val bmr = when (gender.lowercase()) {
        "male" -> 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
        "female" -> 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age)
        else -> throw IllegalArgumentException("Invalid gender. Must be 'male' or 'female'")
    }

    // Activity factor (light activity as default)
    val activityFactor = 1.375

    // Total Daily Energy Expenditure (TDEE)
    val tdee = bmr * activityFactor

    // Average number of steps per day (defaulting to 10,000 steps)
    val averageStepsPerDay = 10000

    // Calculate calories burned per step
    val caloriesPerStep = tdee / averageStepsPerDay

    // Calculate total calories burned for the given number of steps
    return (steps * (caloriesPerStep / weight)).toInt()
}

sealed class BottomNavItem(val title: String, val icon: ImageVector, val route: String) {
    object Chat : BottomNavItem("Messages", Icons.Filled.ChatBubble, "chat")
    object Add : BottomNavItem("Add", Icons.Filled.Add,"add")
    object Map : BottomNavItem("Map", Icons.Filled.Map, "map")
    object Profile : BottomNavItem("Profile", Icons.Filled.Person, "profile")
}
