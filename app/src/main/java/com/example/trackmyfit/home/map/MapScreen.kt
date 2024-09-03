package com.example.trackmyfit.home.map

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    val workoutSpots = remember { mutableStateListOf<WorkoutSpot>() }

    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    // State to manage search visibility
    var isSearchVisible by remember { mutableStateOf(false) }
    val searchItems = listOf("Running", "Cycling", "Rollerblade", "Hiking", "Gym", "Outdoor gym")

    when {
        locationPermissionState.status.isGranted -> {
            // Permission granted, proceed with map setup
            LaunchedEffect(Unit) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLocation = location
                        val userLatLng = LatLng(location.latitude, location.longitude)

                        googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                        googleMap?.addMarker(
                            MarkerOptions()
                                .position(userLatLng)
                                .title("Your Location")
                        )

                        workoutSpots.forEach { spot ->
                            googleMap?.addMarker(
                                MarkerOptions()
                                    .position(LatLng(spot.latitude, spot.longitude))
                                    .title(spot.name)
                            )?.apply {
                                googleMap?.setOnMarkerClickListener { marker ->
                                    if (marker.title == spot.name) {
                                        navController.navigate("show_spot/${spot.id}")
                                    }
                                    true
                                }
                            }
                        }
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

                            workoutSpots.forEach { spot ->
                                googleMap?.addMarker(
                                    MarkerOptions()
                                        .position(LatLng(spot.latitude, spot.longitude))
                                        .title(spot.name)
                                )?.apply {
                                    googleMap?.setOnMarkerClickListener { marker ->
                                        val spot = workoutSpots.find { it.name == marker.title }
                                        if (spot != null) {
                                            navController.navigate("show_spot/${spot.id}")
                                        }
                                        true
                                    }
                                }
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
                isSearchVisible = true  // Svaki put kada se klikne, lista će postati vidljiva
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

        // Display LazyColumn for search items when search is visible
        if (isSearchVisible) {
            LazyColumn(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
                    .background(Color.White)
                    .fillMaxWidth()
            ) {
                items(searchItems) { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Handle click on each item here
                                // Možeš navigirati, filtrirati ili obraditi klik na stavku
                            }
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black)
                        )
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            }
        }
    }
}
