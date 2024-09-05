package com.example.trackmyfit.home.chat
//
//
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.compose.ui.Modifier
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.unit.dp
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.runtime.*
//import androidx.compose.ui.text.input.TextFieldValue
//import coil.compose.rememberAsyncImagePainter
//import com.google.firebase.auth.FirebaseAuth
//import kotlinx.coroutines.launch
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.graphics.Color
//import com.example.trackmyfit.AppNavHost
//import android.util.Log
//
//
//@Composable
//fun ChatListScreen(navController: NavHostController, viewModel: ChatViewModel = viewModel()) {
//    val chatList = viewModel.chatList.collectAsState().value
//    val openDialog = remember { mutableStateOf(false) }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        LazyColumn {
//            items(chatList) { chat ->
//                ChatListItem(chat = chat, onClick = {
//                    navController.navigate("chat/${chat.id}")
//                })
//            }
//        }
//
//        FloatingActionButton(
//            onClick = { openDialog.value = true },
//            backgroundColor = MaterialTheme.colors.primary,
//            modifier = Modifier
//                .align(Alignment.BottomEnd)
//                .padding(16.dp)
//        ) {
//            Icon(Icons.Filled.Add, contentDescription = "New Chat")
//        }
//
//        if (openDialog.value) {
//            NewChatDialog(
//                onDismiss = { openDialog.value = false },
//                onStartChat = { chatId ->
//                    //Log.d("NewChatDialog", "Ovo nije dobro")
//                    navController.navigate("chat/$chatId")
//                },
//                navController = navController  // Prosledi `navController` ovde
//            )
//        }
//    }
//}
//
//@Composable
//fun NewChatDialog(
//    onDismiss: () -> Unit,
//    onStartChat: (String) -> Unit,
//    navController: NavHostController,
//    viewModel: ChatViewModel = viewModel()
//) {
//    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
//    val searchResults by viewModel.searchResults.collectAsState()
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(searchQuery.text) {
//        viewModel.searchUsers(searchQuery.text)
//    }
//
//    Dialog(onDismissRequest = onDismiss) {
//        Surface(
//            shape = MaterialTheme.shapes.medium,
//            color = Color.White,
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Column(modifier = Modifier.padding(16.dp)) {
//                Text(text = "Start New Chat", style = MaterialTheme.typography.h6)
//
//                OutlinedTextField(
//                    value = searchQuery,
//                    onValueChange = { query ->
//                        searchQuery = query
//                    },
//                    label = { Text("Enter name and surname") },
//                    leadingIcon = {
//                        Icon(Icons.Filled.Search, contentDescription = "Search")
//                    },
//                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
//                )
//
//                LazyColumn {
//                    items(searchResults) { user ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable {
//                                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@clickable
//                                    coroutineScope.launch {
//                                        val clickedUserId = user.id
//                                        onDismiss()
//                                        Log.d("NewChatDialog", "Ovo je dobro/$clickedUserId")
//                                        navController.navigate("chat/$clickedUserId")
//
//                                        //Log.d("NewChatDialog", "Navigating to chat/$clickedUserId")
//
//
//                                    }
//                                }
//                                .padding(8.dp),
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Image(
//                                painter = rememberAsyncImagePainter(model = user.profilePictureUrl),
//                                contentDescription = null,
//                                modifier = Modifier.size(48.dp).clip(CircleShape)
//                            )
//                            Spacer(modifier = Modifier.width(16.dp))
//                            Column {
//                                Text(text = "${user.firstName} ${user.lastName}", fontWeight = FontWeight.Bold)
//                            }
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Button(
//                    onClick = onDismiss,
//                    modifier = Modifier.align(Alignment.End)
//                ) {
//                    Text("Cancel")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun ChatListItem(chat: Chat, onClick: () -> Unit) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable(onClick = onClick)
//            .padding(16.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Image(
//            painter = rememberAsyncImagePainter(model = chat.receiverProfilePictureUrl),
//            contentDescription = null,
//            modifier = Modifier.size(48.dp).clip(CircleShape)
//        )
//        Spacer(modifier = Modifier.width(16.dp))
//        Column {
//            Text(
//                text = "${chat.receiverFirstName} ${chat.receiverLastName}",
//                fontWeight = FontWeight.Bold
//            )
//            Text(
//                text = chat.lastMessage,
//                style = if (chat.isSeen) MaterialTheme.typography.body2 else MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
//            )
//        }
//    }
//}