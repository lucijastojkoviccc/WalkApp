package com.example.trackmyfit.home

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import androidx.navigation.NavHostController
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.lifecycle.viewmodel.compose.viewModel


@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }
    val workoutSpots = remember { mutableStateListOf<WorkoutSpot>() }
//    val viewModel: SpotViewModel = viewModel()
//    val workoutSpots by viewModel.workoutSpots.collectAsState()

    // Location permission state
    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)

    // Fetch workout spots from Firestore
    LaunchedEffect(Unit) {
        if (locationPermissionState.status.isGranted) {
            val db = FirebaseFirestore.getInstance()
            val spots = db.collection("workoutspots").get().await()
            workoutSpots.clear()
            workoutSpots.addAll(spots.documents.mapNotNull { it.toObject<WorkoutSpot>() })
        }
    }

    // Update location and set camera zoom to current location
    LaunchedEffect(key1 = locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    val userLatLng = LatLng(location.latitude, location.longitude)

                    // Animiramo kameru tako da se fokusira na trenutnu lokaciju korisnika
                    googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                    // Dodaj marker za trenutnu lokaciju
                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("Your Location")
                            //.snippet("You are here")
                    )

                    // Add markers for all workout spots
                    workoutSpots.forEach { spot ->
                        googleMap?.addMarker(
                            MarkerOptions()
                                .position(LatLng(spot.latitude, spot.longitude))
                                .title(spot.name)
                        )
                    }
                }
            }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (locationPermissionState.status.isGranted) {
            AndroidView(
                factory = { mapView = MapView(context).apply { onCreate(null); onResume() }; mapView!! },
                modifier = Modifier.fillMaxSize(),
                update = { mapView ->
                    mapView.getMapAsync(OnMapReadyCallback { map ->
                        googleMap = map
                        // Zoom controls
                        googleMap?.uiSettings?.isZoomControlsEnabled = true

                        // Add markers for all workout spots
                        workoutSpots.forEach { spot ->
                            googleMap?.addMarker(
                                MarkerOptions()
                                    .position(LatLng(spot.latitude, spot.longitude))
                                    .title(spot.name)
                            )
                        }
                    })
                }
            )
        } else {
            Text(
                text = "Location permission is required to show the map.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        FloatingActionButton(
            onClick = {
                currentLocation?.let { location ->
                    navController.navigate("add_spot/${location.latitude}/${location.longitude}")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Spot",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
