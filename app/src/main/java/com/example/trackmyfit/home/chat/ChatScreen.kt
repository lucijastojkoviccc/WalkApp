package com.example.trackmyfit.home.chat
//package com.example.trackmyfit.home.chat
////import com.example.trackmyfit.home.chat.ChatViewModel.Message
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.compose.ui.Alignment
//import com.google.firebase.auth.FirebaseAuth
//import androidx.navigation.NavHostController
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Send
//import androidx.compose.ui.text.style.TextAlign
//import android.util.Log
//
//
//@Composable
//fun ChatScreen(
//    navController: NavHostController,
//    clickedUserId: String,
//    viewModel: ChatViewModel = viewModel()
//) {
//    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//    val chatId = remember { mutableStateOf<String?>(null) }
//    val receiverName = remember { mutableStateOf("") } // Mutable state for receiver's name
//    //Log.d("ChatScreen", "Receiver Name chat screen AAAAAA: ${clickedUserId}")
//    // Pokrećemo kada se `clickedUserId` promeni
//    Log.d("NewChatDialog", "Ovo ne treba 2 puta")
//    LaunchedEffect(Unit) {
//        chatId.value = viewModel.getOrCreateChat(currentUserId, clickedUserId)
//        val receiver = viewModel.getUserById(clickedUserId) // Fetch the receiver's details
//        receiverName.value = "${receiver.firstName} ${receiver.lastName}" // Set the receiver's name
//
//
//    }
//
//    chatId.value?.let { chat ->
//        val messages = viewModel.getMessagesForChat(chat).collectAsState().value
//        var newMessage by remember { mutableStateOf("") }
//
//        Column(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            // Header with the receiver's name
//            Text(
//                text = receiverName.value,
//                style = MaterialTheme.typography.h6,
//                modifier = Modifier
//                    .padding(16.dp)
//                    .fillMaxWidth(),
//                textAlign = TextAlign.Center
//            )
//            Divider()
//
//            // Scrollable list of messages
//            LazyColumn(
//                modifier = Modifier.weight(1f),
//                contentPadding = PaddingValues(16.dp)
//            ) {
//                items(messages) { message ->
//                    MessageItem(
//                        message = message,
//                        isSender = message.senderId == currentUserId
//                    )
//                }
//            }
//
//            // Input field and send button
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                TextField(
//                    value = newMessage,
//                    onValueChange = { newMessage = it },
//                    modifier = Modifier.weight(1f),
//                    placeholder = { Text("Type a message") }
//                )
//                IconButton(onClick = {
//                    if (newMessage.isNotEmpty()) {
//                        viewModel.sendMessage(chat, newMessage)
//                        newMessage = ""
//                    }
//                }) {
//                    Icon(Icons.Default.Send, contentDescription = "Send")
//                }
//            }
//        }
//    }
//}
//
//
//
//@Composable
//fun MessageItem(message: Message, isSender: Boolean) {
//    val alignment = if (isSender) Alignment.CenterEnd else Alignment.CenterStart
//    val backgroundColor = if (isSender) Color(0xFFDCF8C6) else Color(0xFFFFFFFF)
//    val textColor = if (isSender) Color.Black else Color.Black
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp),
//        contentAlignment = alignment
//    ) {
//        Card(
//            backgroundColor = backgroundColor,
//            modifier = Modifier.padding(8.dp)
//        ) {
//            Text(
//                text = message.message,
//                color = textColor,
//                modifier = Modifier.padding(8.dp),
//                fontWeight = if (message.isSeen) FontWeight.Normal else FontWeight.Bold
//            )
//        }
//    }
//}