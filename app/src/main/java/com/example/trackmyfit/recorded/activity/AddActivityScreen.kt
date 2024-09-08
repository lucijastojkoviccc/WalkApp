package com.example.trackmyfit.recorded.activity
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.trackmyfit.BottomNavItem
import com.example.trackmyfit.home.Home
import android.os.Bundle
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.Text
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddActivityScreen(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Search,
        BottomNavItem.Add,
        BottomNavItem.Map,
        BottomNavItem.Profile
    )
    // State to track if the dialog is open
    val isDialogOpen = remember { mutableStateOf(true) }

    // State to hold the selected activity
    val selectedActivity = remember { mutableStateOf("Running") }

    if (isDialogOpen.value) {
        // Dialog with radio buttons for selecting activity type
        AlertDialog(
            onDismissRequest = { isDialogOpen.value = false },
            title = { Text(text = "Type of Activity") },
            text = {
                Column {
                    Text(
                        text = "Select type of an Activity",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    RadioButtonGroup(selectedActivity.value) { activity ->
                        selectedActivity.value = activity
                    }
                }
            },
            confirmButton = {
                // Save Button Only
                TextButton(onClick = {
                    // Handle the save action
                    isDialogOpen.value = false
                }) {
                    Text("OK")
                }
            }
        )
    }
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
        AddActivityContent( activityType = selectedActivity.value)
    }
}

@Composable
fun RadioButtonGroup(selectedActivity: String, onSelectActivity: (String) -> Unit) {
    val activityOptions = listOf("Running", "Cycling", "Rollerblading")
    val purpleColor = Color(0xFF6200EE) // Define the purple color

    Column {
        activityOptions.forEach { activity ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                // Custom RadioButton with purple color when selected
                RadioButton(
                    selected = (activity == selectedActivity),
                    onClick = { onSelectActivity(activity) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = purpleColor
                    )
                )
                Text(
                    text = activity,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("MissingPermission")
@Composable
fun AddActivityContent(activityType:String, viewModel: StopwatchViewModel = viewModel()) {
    // Context for accessing location services
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var googleMap: GoogleMap? by remember { mutableStateOf(null) }

    var showSummary by remember { mutableStateOf(false) }
    var totalDistance by remember { mutableStateOf(0f) }
    var totalCalories by remember { mutableStateOf(0)}
    // Path points for the route
    val pathPoints = viewModel.pathPoints

    val coroutineScope = rememberCoroutineScope()
    // Fetch the current location
    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            currentLocation = location
        }
    }

    // Update the map when pathPoints changes
    LaunchedEffect(pathPoints) {
        googleMap?.let { map ->
            map.clear()
            val polylineOptions = PolylineOptions().addAll(pathPoints).color(Color.Red.toArgb()).width(5f)
            map.addPolyline(polylineOptions)

            // Optionally, move the camera to the latest point
            pathPoints.lastOrNull()?.let {
                map.moveCamera(CameraUpdateFactory.newLatLng(it))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // MapView at the top
        Box(modifier = Modifier
            .height(300.dp)
            .fillMaxWidth()) {
            AndroidView(factory = { context ->
                MapView(context).apply {
                    onCreate(Bundle())
                    getMapAsync { map ->
                        googleMap = map
                        map.uiSettings.isZoomControlsEnabled = true
                        map.isMyLocationEnabled = true

                        // Pokušaj da se zoom postavi odmah ako je trenutna lokacija već dostupna
                        currentLocation?.let {
                            val currentLatLng = LatLng(it.latitude, it.longitude)
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f)) // Postavi početni zoom
                        }

                        // Ako lokacija nije dostupna odmah, postavi zoom kasnije samo jednom kada dobijemo lokaciju
                        val locationRequest = LocationRequest.create().apply {
                            interval = 5000 // Update every 5 seconds
                            fastestInterval = 2000
                            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        }

                        // Flag da bismo zoomirali samo jednom, na prvu lokaciju
                        var hasZoomedToLocation = false

                        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                            override fun onLocationResult(locationResult: LocationResult) {
                                locationResult.locations.lastOrNull()?.let { location ->
                                    val currentLatLng = LatLng(location.latitude, location.longitude)

                                    if (!hasZoomedToLocation) {
                                        // Postavi početni zoom samo prvi put
                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                                        hasZoomedToLocation = true
                                    } else {
                                        // Premesti kameru bez menjanja zoom-a za kasnije lokacije
                                        map.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng))
                                    }
                                }
                            }
                        }, Looper.getMainLooper())
                    }
                }
            }, update = { mapView ->
                mapView.onResume()

                // Re-draw polyline on map update
                googleMap?.let { map ->
                    map.clear()
                    val polylineOptions = PolylineOptions().addAll(pathPoints).color(Color.Blue.toArgb()).width(5f)
                    map.addPolyline(polylineOptions)

                    // Optionally move camera to the latest point
                    pathPoints.lastOrNull()?.let {
                        map.moveCamera(CameraUpdateFactory.newLatLng(it))
                    }
                }
            })
        }




        // Stopwatch display
        Text(
            text = viewModel.stopwatchTime,
            fontSize = 48.sp,
            color = Color.Black,
            modifier = Modifier.padding(top = 16.dp)
        )

        // Start/Stop/Pause/Cancel/Finish buttons
        if (viewModel.isRunning) {
//            Button(onClick = { viewModel.stopStopwatch() }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EA))) {
//                Text(text = "Stop", color = Color.White)
//            }

            // Show Pause, Cancel, Finish after stopping
            Row(modifier = Modifier.padding(top = 16.dp)) {
                Button(onClick = {
                    viewModel.cancelStopwatch()
                    showSummary = false
                }, modifier = Modifier.padding(4.dp)) {
                    Text(text = "Cancel")
                }
                Button(onClick = {
                    viewModel.finishTracking()
                    totalDistance = viewModel.calculateDistance()

                    // Pokreni korutinu unutar rememberCoroutineScope da pozoveš suspend funkcije
                    coroutineScope.launch {
                        val burnedCalories = viewModel.burnedCalories(viewModel.secondsElapsed, activityType)
                        totalCalories = burnedCalories
                        showSummary = true

                        // Prvo sačuvaj aktivnost i dohvati ID
                        viewModel.saveActivityToDatabase(activityType) { activityId ->
                            googleMap?.snapshot { bitmap ->
                                saveSnapshotToStorage(bitmap!!, activityId) { uri ->
                                    //viewModel.updateActivityWithImage(activityId, uri)
                                }
                            }
                        }
                    }
                }, modifier = Modifier.padding(4.dp)) {
                    Text(text = "Finish")
                }
            }
        }else {
            Button(onClick = { viewModel.startStopwatch(fusedLocationClient) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EA))) {
                Text(text = "Start", color = Color.White)
            }
        }
        if (showSummary) {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally, // Sredi sadržaj po sredini
                verticalArrangement = Arrangement.spacedBy(8.dp) // Veći razmak između redova
            ) {
                Text(
                    text = "Time: ${viewModel.stopwatchTime}",
                    fontSize = 24.sp, // Veći font za lepši prikaz
                    color = Color.Black,
                    fontWeight = FontWeight.Medium, // Bold tekst za veći naglasak
                    modifier = Modifier.padding(bottom = 8.dp) // Razmak ispod teksta
                )

                Text(
                    text = "Distance: %.2f km".format(totalDistance),
                    fontSize = 22.sp, // Veći font za distancu
                    color = Color.Black,
                    fontWeight = FontWeight.Medium, // Srednja debljina za tekst
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Calories: $totalCalories",
                    fontSize = 22.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}
fun saveSnapshotToStorage(bitmap: Bitmap, activityId: String, onSuccess: (Uri) -> Unit) {
    val storageRef = FirebaseStorage.getInstance().reference
    val snapshotRef = storageRef.child("snapshots/${activityId}.jpg")  // Koristimo id aktivnosti za ime slike

    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()

    val uploadTask = snapshotRef.putBytes(data)
    uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let { throw it }
        }
        snapshotRef.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val downloadUri = task.result
            onSuccess(downloadUri)  // Vraćamo Uri slike
        } else {
            // Handle failure
        }
    }
}






