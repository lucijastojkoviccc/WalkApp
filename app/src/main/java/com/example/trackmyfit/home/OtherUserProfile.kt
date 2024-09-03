package com.example.trackmyfit.home
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import coil.compose.rememberImagePainter
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.tasks.await
//import androidx.navigation.NavController
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.material.Text
//import androidx.compose.foundation.lazy.items
//import java.util.*
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.google.firebase.auth.FirebaseAuth
//
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.Card
//import androidx.compose.material.Text
//import androidx.compose.runtime.Composable
//import kotlinx.coroutines.launch
//
//
//@Composable
//fun OtherUserProfileScreen(navController: NavController, userId: String, viewModel: ChatViewModel = viewModel()) {
//    var userData by remember { mutableStateOf<UserData?>(null) }
//    var isLoading by remember { mutableStateOf(true) }
//    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//    val coroutineScope = rememberCoroutineScope()
//    // Fetch user data
//    LaunchedEffect(userId) {
//        val db = FirebaseFirestore.getInstance()
//        val userDocument = db.collection("users").document(userId).get().await()
//        userData = userDocument.toObject(UserData::class.java)
//        isLoading = false
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("User Profile") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        if (isLoading) {
//            Box(modifier = Modifier.fillMaxSize()) {
//                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//            }
//        } else {
//            userData?.let { user ->
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(paddingValues)
//                        .padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    // Profilna slika
//                    Image(
//                        painter = rememberImagePainter(user.profilePictureUrl),
//                        contentDescription = "Profile Picture",
//                        modifier = Modifier
//                            .size(128.dp)
//                            .clip(CircleShape),
//                        contentScale = ContentScale.Crop
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Ime i prezime sa ikonicom za poruke
//                    Row(
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.Center
//                    ) {
//                        Text(
//                            text = "${user.firstName} ${user.lastName}",
//                            fontWeight = FontWeight.Bold,
//                            fontSize = 24.sp
//                        )
//
//                        Spacer(modifier = Modifier.width(8.dp))
//
//                        IconButton(onClick = {
//                            coroutineScope.launch { // Pokretanje coroutine-a
//                                val chatId = viewModel.getOrCreateChat(currentUserId, userId)
//                                navController.navigate("chat/$chatId")
//                            }
//                        }) {
//                            Icon(Icons.Default.Message, contentDescription = "Message Icon")
//                        }
//
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    Text(
//                        text = "${user.gender}",
//                        fontSize = 14.sp
//                    )
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Visina i težina
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 16.dp),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(text = "Visina: ${user.height} cm", fontSize = 16.sp)
//                        Text(text = "Težina: ${user.weight} kg", fontSize = 16.sp)
//                    }
//
//                    Spacer(modifier = Modifier.height(16.dp))
//
//                    // Activities section
//                    Text(
//                        text = "Activities",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 20.sp,
//                        modifier = Modifier.padding(bottom = 8.dp)
//                    )
//
//                    // Lazy list for activities
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 16.dp)
//                    ) {
//                        items(10) { index -> // Replace 10 with actual activity data later
//                            ActivityItem(activityName = "Activity #$index")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//
//@Composable
//fun ActivityItem(activityName: String) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        elevation = 4.dp
//    ) {
//        Text(
//            text = activityName,
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth(),
//            fontSize = 16.sp
//        )
//    }
//}
