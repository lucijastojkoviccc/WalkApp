package com.example.trackmyfit.home
import android.content.pm.PackageManager
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
import com.example.trackmyfit.recorded.walk.StepCounterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date



@Composable
fun MainScreen(navController: NavHostController) {


    val stepCounterViewModel: StepCounterViewModel = viewModel()

    Scaffold(

    ) { innerPadding ->
        Home(modifier = Modifier.padding(innerPadding))
        HomeScreenContent(viewModel = stepCounterViewModel)
    }
}

@Composable
fun Home(modifier: Modifier = Modifier) {
    Text(text = "")
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    viewModel: StepCounterViewModel = viewModel()
) {

    // State to control the dialog visibility
    var showFallDialog by remember { mutableStateOf(false) }

    // Fall detection system
    FallDetectionSystem(
        onFallDetected = { showFallDialog = true }
    )

    // Show dialog when fall is detected
    if (showFallDialog) {
        ShowFallDialog(
            onYesClick = {
                showFallDialog = false
                // Logic if user is okay
            },
            onNoClick = {
                showFallDialog = false
                // Logic if user is not okay
            }
        )
    }

    val scope = rememberCoroutineScope()
    val stepCount by viewModel.stepCount.collectAsState()


    var permissionGranted by remember { mutableStateOf(false) }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        RequestPermission(permission = android.Manifest.permission.ACTIVITY_RECOGNITION) {
            permissionGranted = true
        }
    } else {
        permissionGranted = true
    }

    if (permissionGranted) {
        DisposableEffect(Unit) {
            viewModel.registerSensor()
            onDispose { viewModel.unregisterSensor() }
        }
    }
    val distanceWalked = viewModel.calculateDistanceWalked(stepCount, 1.75f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, top = 20.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        scope.launch {
                            saveWalkData(
                                stepCount = stepCount,

                                distance = distanceWalked
                            )
                        }
                        viewModel.resetStepCount()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressBar(
                progress = (stepCount / 10000f) * 100,
                modifier = Modifier.size(250.dp).padding(top=40.dp),
                color = Color(0xFF29CF1D),
                strokeWidth = 20f
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                Text(
                    text = "$stepCount steps",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF29CF1D)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format("%.2f km", distanceWalked),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF29CF1D)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding( top = 170.dp)
        ) {
            WalkingTabContent()
        }
    }
}

fun saveWalkData(stepCount: Int, distance: Float) {
    val user = FirebaseAuth.getInstance().currentUser
    val userId = user?.uid ?: return
    val db = FirebaseFirestore.getInstance()

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    val walkData = hashMapOf(
        "userId" to userId,
        "steps" to stepCount,
        "distance" to distance,
        "date" to currentDate
    )

    db.collection("walks")
        .add(walkData)
        .addOnSuccessListener {
            Log.d("HomeScreenContent", "Walk data successfully saved!")
        }
        .addOnFailureListener { e ->
            Log.e("HomeScreenContent", "Error saving walk data", e)
        }
}


@Composable
fun CircularProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD7BDE2),
    strokeWidth: Float = 20f
) {
    Canvas(modifier = modifier) {
        val sweepAngle = (progress / 100f) * 360f
        drawCircle(
            color = color.copy(alpha = 0.3f),
            style = Stroke(strokeWidth)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(strokeWidth)
        )
    }
}

@Composable
fun RequestPermission(
    permission: String,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    onPermissionGranted()
                } else {
                    Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        )

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(permission)
            } else {
                onPermissionGranted()
            }
        }
    } else {
        onPermissionGranted()
    }
}