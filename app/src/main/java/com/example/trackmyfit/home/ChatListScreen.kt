package com.example.trackmyfit.home
import  com.example.trackmyfit.home.ChatViewModel.Chat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import androidx.compose.material.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.MaterialTheme
import coil.compose.rememberAsyncImagePainter


@Composable
fun ChatListScreen(navController: NavHostController, viewModel: ChatViewModel = viewModel()) {
    val chatList = viewModel.chatList.collectAsState().value

    LazyColumn {
        items(chatList) { chat ->
            ChatListItem(chat = chat, onClick = {
                navController.navigate("chat/${chat.id}")
            })
        }
    }
}

@Composable
fun ChatListItem(chat: ChatViewModel.Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = chat.receiverProfilePictureUrl),
            contentDescription = null,
            modifier = Modifier.size(48.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "${chat.receiverFirstName} ${chat.receiverLastName}",
                fontWeight = FontWeight.Bold
            )
            Text(
                text = chat.lastMessage,
                style = if (chat.isSeen) MaterialTheme.typography.body2 else MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}



