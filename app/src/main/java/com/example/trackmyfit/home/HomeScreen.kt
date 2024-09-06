package com.example.trackmyfit.home
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.trackmyfit.BottomNavItem
import com.example.trackmyfit.recorded.SleepViewModel
import com.example.trackmyfit.recorded.walk.StepCounterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.widget.Toast

@Composable
fun MainScreen(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Search,
        BottomNavItem.Add,
        BottomNavItem.Map,
        BottomNavItem.Profile
    )

    val stepCounterViewModel: StepCounterViewModel = viewModel()
    val sleepViewModel: SleepViewModel = viewModel()
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
                                popUpTo(navController.graph.startDestinationRoute ?: "home") {
                                    saveState = true
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
        // Prikaz osnovnog sadržaja HomeScreen-a (može biti prazan, ili neki intro tekst)
        Home(modifier = Modifier.padding(innerPadding))
        HomeScreenContent(
            viewModel = stepCounterViewModel,
            sleepViewModel = sleepViewModel)
    }
}
@Composable
fun Home(modifier: Modifier = Modifier) {
    Text(text = "")
}
@Composable
fun HomeScreenContent(
    viewModel: StepCounterViewModel = viewModel(),
    sleepViewModel: SleepViewModel = viewModel()
) {
    val context = LocalContext.current
//    val stepCount by viewModel.stepCount.collectAsState()
    val scope = rememberCoroutineScope()

    val stepCount by viewModel.stepCount.collectAsState()

    // State za čuvanje podataka o korisniku
    var weight by remember { mutableStateOf(70f) }
    var height by remember { mutableStateOf(170) }
    var heightinm by remember { mutableStateOf(1.7f) }
    var gender by remember { mutableStateOf("male") }
    var birthday by remember { mutableStateOf("") }
    var age by remember { mutableStateOf(0) }

    // State za spavanje
    var sleepStart by remember { mutableStateOf<Long?>(null) }
    var sleepEnd by remember { mutableStateOf<Long?>(null) }
    var sleepLength by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return@LaunchedEffect
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                weight = document.getDouble("weight")?.toFloat() ?: 70f
                height = document.getDouble("height")?.toInt() ?: 170
                gender = document.getString("gender") ?: "male"
                birthday = document.getString("birthday") ?: ""

                heightinm = height / 100f
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

    val caloriesBurned = viewModel.calculateCaloriesBurned(stepCount, weight, heightinm, age, gender)
    val distanceWalked = viewModel.calculateDistanceWalked(stepCount, heightinm, gender)

    // Postavljanje sadržaja sa skrolovanjem
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Naslov "Walking" iznad ljubičastog kruga
        Text(
            text = "Walking",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 8.dp, top=10.dp)
        )

        // Ljubicasti krug sa podacima unutar njega
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .padding(top = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(250.dp)) {
                drawCircle(
                    color = Color(0xFFD7BDE2), // Svetlo ljubičasta boja
                    style = Stroke(width = 20f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$stepCount steps",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$caloriesBurned kcal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$distanceWalked km",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.Gray
                )
            }
        }

        // Sekcija za spavanje
        Text(
            text = "Sleeping",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(
                    color = Color.Black,
                    style = Stroke(width = 20f)
                )
            }
            Text(
                text = if (sleepLength.isNotEmpty()) sleepLength else "0h 0min",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD7BDE2)
            )
        }

        // Buttons for Good Night and Good Morning
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    sleepStart = System.currentTimeMillis()
                    sleepEnd = null
                    sleepLength = ""

                    Toast.makeText(context, "Sleep tight!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
            ) {
                Text(text = "Good night!", color = Color(0xFFD7BDE2))
            }

            Button(
                onClick = {
                    sleepEnd = System.currentTimeMillis()
                    if (sleepStart != null && sleepEnd != null) {
                        val sleepDuration = sleepEnd!! - sleepStart!!
                        if (sleepDuration <= 0) {
                            Log.e("SleepViewModel", "Error: Invalid sleep duration.")
                            return@Button
                        }
                        val hours = TimeUnit.MILLISECONDS.toHours(sleepDuration)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(sleepDuration) % 60
                        sleepLength = "${hours}h ${minutes}min"

                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@Button
                        sleepViewModel.saveSleepSession(sleepStart!!, sleepEnd!!, sleepLength, userId)
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFD7BDE2))
            ) {
                Text(text = "Good Morning!", color = Color.Black)
            }
        }
    }
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
