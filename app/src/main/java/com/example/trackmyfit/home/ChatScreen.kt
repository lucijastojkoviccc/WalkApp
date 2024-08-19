package com.example.trackmyfit.home
import com.example.trackmyfit.home.ChatViewModel.Message
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.NavHostController
import androidx.compose.foundation.text.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send




@Composable
fun ChatScreen(navController: NavHostController, chatId: String, viewModel: ChatViewModel = viewModel()) {
    val messages = viewModel.getMessagesForChat(chatId).collectAsState().value
    var newMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(messages) { message ->
                MessageItem(
                    message = message,
                    isSender = message.senderId == FirebaseAuth.getInstance().currentUser?.uid
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") }
            )
            IconButton(onClick = {
                if (newMessage.isNotEmpty()) {
                    viewModel.sendMessage(chatId, newMessage)
                    newMessage = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, isSender: Boolean) {
    val alignment = if (isSender) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isSender) Color(0xFFDCF8C6) else Color(0xFFFFFFFF)
    val textColor = if (isSender) Color.Black else Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Card(
            backgroundColor = backgroundColor,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = message.message,
                color = textColor,
                modifier = Modifier.padding(8.dp),
                fontWeight = if (message.isSeen) FontWeight.Normal else FontWeight.Bold
            )
        }
    }
}
