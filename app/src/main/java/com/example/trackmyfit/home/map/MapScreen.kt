package com.example.trackmyfit.home.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.accompanist.permissions.shouldShowRationale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.model.Marker

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    val handler = Handler(Looper.getMainLooper())
    var lastClickedMarker: Marker? = null
    var isDoubleClick = false
    // Originalna lista workout spotova
    val allWorkoutSpots = remember { mutableStateListOf<WorkoutSpot>() }
    // Filtrirana lista workout spotova koja će se prikazati na mapi
    var filteredWorkoutSpots by remember { mutableStateOf(listOf<WorkoutSpot>()) }

    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    // State to manage search visibility
    var isSearchVisible by remember { mutableStateOf(false) }
    val searchItems = listOf("Running", "Cycling", "Rollerblade", "Hiking", "Gym", "Outdoor gym")
    val selectedItems = remember { mutableStateListOf<String>() }

    // Funkcija za učitavanje podataka iz Firestore-a
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("workoutspots")
            .get()
            .addOnSuccessListener { documents ->
                val spots = documents.mapNotNull {
                    it.toObject(WorkoutSpot::class.java).apply { id = it.id }  // Preuzmi ID iz Firestore-a
                }
                allWorkoutSpots.clear()
                allWorkoutSpots.addAll(spots)
                filteredWorkoutSpots = spots

                // Prikazivanje svih workout spotova
                googleMap?.clear()

                // Prikaz trenutne lokacije korisnika
                currentLocation?.let { location ->
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("Your Location")
                    )
                }

                // Prikazivanje svih workout spotova
                allWorkoutSpots.forEach { spot ->
                    val marker = googleMap?.addMarker(
                        MarkerOptions()
                            .position(LatLng(spot.latitude, spot.longitude))
                            .title(spot.name)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)) // Ljubičasti pin
                    )
                    marker?.tag = spot.id  // Dodeljivanje ID kao tag markera
                }
            }
    }

    when {
        locationPermissionState.status.isGranted -> {
            // Permission granted, proceed with map setup
            LaunchedEffect(Unit) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = location
                        val userLatLng = LatLng(location.latitude, location.longitude)

                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                        // Prikaz trenutne lokacije odmah po otvaranju mape
                        googleMap?.addMarker(
                            MarkerOptions()
                                .position(userLatLng)
                                .title("Your Location")
                        )
                    }
                }
            }
        }
        locationPermissionState.status.shouldShowRationale || !locationPermissionState.status.isGranted -> {
            // Show some UI explaining that location permission is required
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Location permission is required to use the map. Please enable it in settings.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Pokušaj traženja dozvole
            LaunchedEffect(Unit) {
                locationPermissionState.launchPermissionRequest()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissionState.status.isGranted) {
            val lifecycleOwner = LocalLifecycleOwner.current
            AndroidView(
                factory = {
                    mapView = MapView(context).apply {
                        onCreate(null)
                        lifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_START -> onStart()
                                Lifecycle.Event.ON_RESUME -> onResume()
                                Lifecycle.Event.ON_PAUSE -> onPause()
                                Lifecycle.Event.ON_STOP -> onStop()
                                Lifecycle.Event.ON_DESTROY -> onDestroy()
                                else -> {}
                            }
                        })
                        getMapAsync { map ->
                            googleMap = map
                            googleMap?.uiSettings?.isZoomControlsEnabled = true

                            // Prikazivanje svih workout spotova i trenutne lokacije
                            googleMap?.clear()

                            // Prikaz trenutne lokacije korisnika
                            currentLocation?.let { location ->
                                val userLatLng = LatLng(location.latitude, location.longitude)
                                googleMap?.addMarker(
                                    MarkerOptions()
                                        .position(userLatLng)
                                        .title("Your Location")
                                )
                            }

                            // Prikaz filtriranih workout spotova
                            filteredWorkoutSpots.forEach { spot ->
                                val marker = googleMap?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(spot.latitude, spot.longitude))
                                        .title(spot.name)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)) // Ljubičasti pin
                                )
                                marker?.tag = spot.id  // Dodeljivanje ID kao tag markera
                            }

                            // Postavljanje klik listenera za markere
                            googleMap?.setOnMarkerClickListener { marker ->
                                val spotId = marker.tag as? String
                                if (spotId != null) {
                                    navController.navigate("show_spot/$spotId") // Navigacija na ShowSpotScreen sa prosleđenim id-em
                                }
                                marker.hideInfoWindow() // Ručno zatvori InfoWindow nakon klika
                                true     // Vrati true da spreči InfoWindow
                            }
                        }
                    }
                    mapView!!
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // FloatingActionButton for search
        FloatingActionButton(
            onClick = {
                isSearchVisible = true  // Svaki put kada se klikne, dijalog će postati vidljiv
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 90.dp, start = 16.dp),  // Postavlja dugme u donji levi ugao iznad "+" dugmeta
            containerColor = MaterialTheme.colorScheme.primary  // Ista boja kao "+" dugme
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // FloatingActionButton for adding a new spot
        FloatingActionButton(
            onClick = {
                currentLocation?.let { location ->
                    navController.navigate("add_spot/${location.latitude}/${location.longitude}")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary  // Ista boja kao dugme za pretragu
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Spot",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        // Display dialog for search items when search is visible
        if (isSearchVisible) {
            AlertDialog(
                onDismissRequest = { isSearchVisible = false },
                title = { Text(text = "Filter Activities") },
                text = {
                    LazyColumn {
                        items(searchItems) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedItems.contains(item),
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            selectedItems.add(item)
                                        } else {
                                            selectedItems.remove(item)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = item)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Implement filter logic here
                            filteredWorkoutSpots = allWorkoutSpots.filter { spot ->
                                spot.activities.any { it in selectedItems }
                            }

                            // Zatvori dijalog nakon filtriranja
                            isSearchVisible = false

                            // Očisti sve markere sa mape i dodaj nove filtrirane markere
                            googleMap?.clear()

                            // Prikaz trenutne lokacije korisnika
                            currentLocation?.let { location ->
                                val userLatLng = LatLng(location.latitude, location.longitude)
                                googleMap?.addMarker(
                                    MarkerOptions()
                                        .position(userLatLng)
                                        .title("Your Location")
                                )
                            }

                            // Prikaz filtriranih workout spotova
                            filteredWorkoutSpots.forEach { spot ->
                                val marker = googleMap?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(spot.latitude, spot.longitude))
                                        .title(spot.name)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)) // Ljubičasti pin
                                )
                                marker?.tag = spot.id  // Dodeljivanje ID kao tag markera
                            }

                            // Postavljanje klik listenera za markere
                            googleMap?.setOnMarkerClickListener { marker ->
                                val spotId = marker.tag as? String
                                if (spotId != null) {
                                    navController.navigate("show_spot/$spotId") // Navigacija na ShowSpotScreen sa prosleđenim id-em
                                }
                                marker.hideInfoWindow() // Ručno zatvori InfoWindow nakon klika
                                true     // Vrati true da zadrži standardno ponašanje markera
                            }
                        }
                    ) {
                        Text("Filter")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { isSearchVisible = false }
                    ) {
                        Text("Cancel")
                    }
                },
                properties = DialogProperties(dismissOnClickOutside = false)
            )
        }
    }
}
