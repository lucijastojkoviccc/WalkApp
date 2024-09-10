package com.example.trackmyfit.home.leaderboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.trackmyfit.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.trackmyfit.BottomNavItem
import com.example.trackmyfit.home.Home
import com.example.trackmyfit.home.HomeScreenContent
import com.example.trackmyfit.home.search.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LB(navController: NavController)
{
    val items = listOf(
        BottomNavItem.Search,
        BottomNavItem.Leaderboard,
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
                        icon = { androidx.compose.material.Icon(imageVector = item.icon, contentDescription = item.title) },
                        label = { androidx.compose.material.Text(text = item.title) },
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
        LeaderboardScreen(navController = navController)
    }
}

@Composable
fun LeaderboardScreen(navController: NavController) {

    val viewModel: LeaderboardViewModel = viewModel()
    var selectedTab by remember { mutableStateOf("Running") }

    // Trigger the data fetch as soon as the screen is opened
    LaunchedEffect(Unit) {
        viewModel.fetchLeaderboard("Running")
    }

    Column {
        Spacer(modifier = Modifier.height(30.dp))
        // Tab Row with icons
        TabRow(
            selectedTabIndex = when (selectedTab) {
                "Running" -> 0
                "Cycling" -> 1
                else -> 2
            }
        ) {
            Tab(
                selected = selectedTab == "Running",
                onClick = { selectedTab = "Running"; viewModel.fetchLeaderboard("Running") }//,
                //icon = { Icon(painterResource(id = R.drawable.ic_running), contentDescription = "Running") }
            )
            {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_running),
                        contentDescription = "Running",
                        modifier = Modifier.size(24.dp) // Smaller icon size
                    )
                    Text(text = "Running", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Tab(
                selected = selectedTab == "Cycling",
                onClick = { selectedTab = "Cycling"; viewModel.fetchLeaderboard("Cycling") }//,
                //icon = { Icon(painterResource(id = R.drawable.ic_cycling), contentDescription = "Cycling") }
            )
            {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cycling),
                        contentDescription = "Cycling",
                        modifier = Modifier.size(24.dp) // Smaller icon size
                    )
                    Text(text = "Cycling", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Tab(
                selected = selectedTab == "Rollerblading",
                onClick = { selectedTab = "Rollerblading"; viewModel.fetchLeaderboard("Rollerblading") }//,
                //icon = { Icon(painterResource(id = R.drawable.ic_rollerblading), contentDescription = "Rollerblading") }
            )
            {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rollerblading),
                        contentDescription = "Rollerblading",
                        modifier = Modifier.size(24.dp) // Smaller icon size
                    )
                    Text(text = "Rollerblading", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // Display leaderboard list or empty message
        if (viewModel.leaderboardState.isEmpty()) {
            Text("No activities found", modifier = Modifier.padding(16.dp))
        } else {
            LazyColumn {
                itemsIndexed(viewModel.leaderboardState) { index, (userId, distanceInKM) ->
                    LeaderboardItem(userId = userId, distanceInKM = distanceInKM.toString(), rank = index + 1, navController = navController)
                }
            }
        }
    }
}




@Composable
fun LeaderboardItem(userId: String, distanceInKM: String, rank: Int, navController: NavController) {
    var user by remember { mutableStateOf<User?>(null) }

    // Fetch the user data from Firestore based on userId
    LaunchedEffect(userId) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                user = document.toObject(User::class.java)?.copy(id = document.id)
            }
    }

    // If user data is still loading, show a loading indicator
    if (user == null) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
    } else {
        Column {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (userId == currentUserId) {
                            // Ako je ID trenutnog korisnika, idi na njegov profil
                            navController.navigate(BottomNavItem.Profile.route)
                        } else {
                            // Ako je ID drugog korisnika, idi na profil drugog korisnika
                            navController.navigate("otherUserProfile/$userId")
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Display the rank
                Text(
                    text = "$rank.",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Profile image
                Image(
                    painter = rememberImagePainter(user?.profilePictureUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Display user's name
                Text("${user?.firstName} ${user?.lastName}", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.weight(1f))

                // Display the distance in KM
                Text(
                    text = String.format("%.2f km", distanceInKM.toDouble()),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Add a Divider below each item
            Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        }
    }
}



