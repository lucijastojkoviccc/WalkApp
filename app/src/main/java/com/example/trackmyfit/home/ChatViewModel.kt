package com.example.trackmyfit.home
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.auth.FirebaseAuth

import android.util.Log
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.trackmyfit.MainActivity
import com.example.trackmyfit.R
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _chatList = MutableStateFlow<List<Chat>>(emptyList())
    val chatList: StateFlow<List<Chat>> get() = _chatList

    init {
        fetchChatsInRealTime()
    }

    // Fetching chat list
    private fun fetchChatsInRealTime() {
        db.collection("chats").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChatViewModel", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val chatListTemp = mutableListOf<Chat>()

                snapshot.documents.forEach { document ->
                    val chatId = document.id
                    val userIds = document.get("userIds") as? List<String> ?: return@forEach
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@forEach

                    // Pronalaženje receiver-a (drugi korisnik)
                    val receiverId = userIds.firstOrNull { it != currentUserId } ?: return@forEach

                    // Dohvatanje podataka o receiver-u
                    db.collection("users").document(receiverId).get()
                        .addOnSuccessListener { userDocument ->
                            val chat = Chat(
                                id = chatId,
                                userIds = userIds,
                                lastMessage = document.getString("lastMessage") ?: "",
                                receiverFirstName = userDocument.getString("firstName") ?: "",
                                receiverLastName = userDocument.getString("lastName") ?: "",
                                receiverProfilePictureUrl = userDocument.getString("profilePictureUrl") ?: "",
                                isSeen = document.getBoolean("isSeen") ?: true
                            )
                            chatListTemp.add(chat)

                            // Ažuriramo listu chatova kada se svi korisnici učitaju
                            if (chatListTemp.size == snapshot.size()) {
                                _chatList.value = chatListTemp
                            }
                        }
                }
            } else {
                Log.d("ChatViewModel", "No chat data found.")
            }
        }
    }


    // Fetching messages for a specific chat
    fun getMessagesForChat(chatId: String): StateFlow<List<Message>> {
        val messagesFlow = MutableStateFlow<List<Message>>(emptyList())
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messagesFlow.value = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
                }
            }
        return messagesFlow
    }

    // Sending message
    fun sendMessage(chatId: String, messageText: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val newMessageRef = db.collection("chats").document(chatId).collection("messages").document()

        val message = Message(
            messageId = newMessageRef.id,
            senderId = currentUser.uid,
            receiverId = "", // You should set this dynamically based on the chat
            message = messageText,
            isSeen = false,
            timestamp = System.currentTimeMillis()
        )

        newMessageRef.set(message)
        db.collection("chats").document(chatId).update("lastMessage", messageText)
    }

    // Updating seen status
    fun updateMessageSeen(chatId: String, messageId: String) {
        db.collection("chats").document(chatId).collection("messages").document(messageId)
            .update("isSeen", true)
    }
    suspend fun getOrCreateChat(currentUserId: String, otherUserId: String): String {
        val db = FirebaseFirestore.getInstance()
        val chatCollection = db.collection("chats")

        // Pronađi postojeći chat između ova dva korisnika
        val existingChat = chatCollection
            .whereArrayContains("userIds", currentUserId)
            .get()
            .await()
            .documents
            .firstOrNull { it.toObject(Chat::class.java)?.userIds?.contains(otherUserId) == true }

        return if (existingChat != null) {
            existingChat.id // Postojeći chat
        } else {
            // Kreiraj novi chat
            val newChatRef = chatCollection.document()
            val newChat = Chat(
                id = newChatRef.id,
                userIds = listOf(currentUserId, otherUserId),
                lastMessage = "",
                timestamp = System.currentTimeMillis()
            )
            newChatRef.set(newChat).await()
            newChatRef.id
        }
    }


    data class Chat(
        val id: String = "",
        val userIds: List<String> = emptyList(),
        val lastMessage: String = "",
        val receiverFirstName: String = "",
        val receiverLastName: String = "",
        val receiverProfilePictureUrl: String = "",
        val isSeen: Boolean = true,
        val timestamp: Long = System.currentTimeMillis()
    )


    data class Message(
        val messageId: String = "",
        val senderId: String = "",
        val receiverId: String = "",
        val message: String = "",
        val isSeen: Boolean = false,
        val timestamp: Long = System.currentTimeMillis()
    )
}
