package com.example.trackmyfit.home.search
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*  // This allows you to access filled icons
import androidx.compose.material.icons.outlined.*  // This allows you to access outlined icons
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import coil.compose.rememberImagePainter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.input.TextFieldValue
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.Color
import com.example.trackmyfit.AppNavHost
import android.util.Log
import androidx.compose.foundation.lazy.items
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.trackmyfit.BottomNavItem


@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = viewModel()
) {
    val items = listOf(
        BottomNavItem.Search,
        BottomNavItem.Leaderboard,
        BottomNavItem.Add,
        BottomNavItem.Map,
        BottomNavItem.Profile
    )

    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val searchResults: List<User> = viewModel.searchResults.collectAsState().value
    val coroutineScope = rememberCoroutineScope()

    // Trigger search when the query changes
    LaunchedEffect(searchQuery.text) {
        viewModel.searchUsers(searchQuery.text)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Search Users", color = Color.White) },
                backgroundColor = Color(0xFF6200EE)
            )},
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
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)) {

            // Title
            //Text(text = "Search Users", style = MaterialTheme.typography.h6)

            // Search input field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                },
                label = { Text("Enter name and surname") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            val currentUser = FirebaseAuth.getInstance().currentUser
            val currentUserId = currentUser?.uid

            if (searchResults.isNotEmpty()) {
                // Lazy list of search results
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults) { user ->
                        UserListItem(user = user, onClick = {
                            coroutineScope.launch {
                                if (user.id == currentUserId) {
                                    // Ako je kliknuto na sopstveni profil, idi na Profile screen
                                    navController.navigate(BottomNavItem.Profile.route)
                                } else {
                                    // Ako je kliknuto na drugi profil, idi na otherUserProfile
                                    navController.navigate("otherUserProfile/${user.id}")
                                }
                            }
                        })
                    }
                }
            } else {
                // Display when no users are found
                Text(text = "No users found", modifier = Modifier.padding(top = 16.dp))
            }
        }
    }
}





@Composable
fun UserListItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile picture
        Image(
            painter = rememberAsyncImagePainter(model = user.profilePictureUrl),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))

        // User name
        Column {
            Text(
                text = "${user.firstName} ${user.lastName}",
                fontWeight = FontWeight.Bold
            )
        }
    }
}




