package com.example.trackmyfit.home.chat
//package com.example.trackmyfit.home.chat
//
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.ValueEventListener
//import androidx.lifecycle.ViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import com.google.firebase.auth.FirebaseAuth
//import android.util.Log
//import kotlinx.coroutines.tasks.await
//import androidx.lifecycle.viewModelScope
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.launch
//
//data class User(
//    val id: String = "",
//    val firstName: String = "",
//    val lastName: String = "",
//    val profilePictureUrl: String = ""
//)
//
//data class Chat(
//    val id: String = "",
//    val userIds: Map<String, Boolean> = emptyMap(),
//    val lastMessage: String = "",
//    val receiverFirstName: String = "",
//    val receiverLastName: String = "",
//    val receiverProfilePictureUrl: String = "",
//    val isSeen: Boolean = true,
//    val timestamp: Long = System.currentTimeMillis()
//)
//
//data class Message(
//    val messageId: String = "",
//    val senderId: String = "",
//    val receiverId: String = "",
//    val message: String = "",
//    val isSeen: Boolean = false,
//    val timestamp: Long = System.currentTimeMillis()
//)
//
//class ChatViewModel : ViewModel() {
//    private val db = FirebaseDatabase.getInstance().reference
//    private val _chatList = MutableStateFlow<List<Chat>>(emptyList())
//    val chatList: StateFlow<List<Chat>> get() = _chatList
//
//    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
//    val searchResults: StateFlow<List<User>> get() = _searchResults
//
//    private val firestore = FirebaseFirestore.getInstance()
//
//    init {
//        fetchChatsInRealTime()
//    }
//
//    // Fetching chat list from Realtime Database
//    private fun fetchChatsInRealTime() {
//        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
//        db.child("chats").orderByChild("userIds/$currentUserId").equalTo(true)
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val chatListTemp = mutableListOf<Chat>()
//                    snapshot.children.forEach { chatSnapshot ->
//                        val chat = chatSnapshot.getValue(Chat::class.java)
//                        if (chat != null) {
//                            chatListTemp.add(chat)
//                        }
//                    }
//                    _chatList.value = chatListTemp
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("ChatViewModel", "Failed to fetch chats", error.toException())
//                }
//            })
//    }
//
//    // Fetching messages for a specific chat
//    fun getMessagesForChat(chatId: String): StateFlow<List<Message>> {
//        val messagesFlow = MutableStateFlow<List<Message>>(emptyList())
//        db.child("chats").child(chatId).child("messages").orderByChild("timestamp")
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    val messagesList = snapshot.children.mapNotNull { it.getValue(Message::class.java) }
//                    messagesFlow.value = messagesList
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("ChatViewModel", "Failed to fetch messages", error.toException())
//                }
//            })
//        return messagesFlow
//    }
//
//    // Sending message to Realtime Database
//    fun sendMessage(chatId: String, messageText: String) {
//        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
//        val newMessageRef = db.child("chats").child(chatId).child("messages").push()
//
//        val message = Message(
//            messageId = newMessageRef.key ?: "",
//            senderId = currentUser.uid,
//            receiverId = "", // You should set this dynamically based on the chat
//            message = messageText,
//            isSeen = false,
//            timestamp = System.currentTimeMillis()
//        )
//
//        newMessageRef.setValue(message)
//        db.child("chats").child(chatId).child("lastMessage").setValue(messageText)
//    }
//
//    // Updating seen status for messages
//    fun updateMessageSeen(chatId: String, messageId: String) {
//        db.child("chats").child(chatId).child("messages").child(messageId).child("isSeen").setValue(true)
//    }
//
//    // Searching users by name
//    fun searchUsers(query: String) {
//        viewModelScope.launch {
//            firestore.collection("users")
//                .get()
//                .addOnSuccessListener { documents ->
//                    val users = documents.mapNotNull { document ->
//                        val firstName = document.getString("firstName") ?: ""
//                        val lastName = document.getString("lastName") ?: ""
//                        val profilePictureUrl = document.getString("profilePictureUrl") ?: ""
//                        val id = document.id
//
//                        if (firstName.contains(query, ignoreCase = true) || lastName.contains(query, ignoreCase = true)) {
//                            User(id, firstName, lastName, profilePictureUrl)
//                        } else {
//                            null
//                        }
//                    }
//                    _searchResults.value = users
//                }
//        }
//    }
//
//    // Get or create chat (used when starting a new conversation)
//    suspend fun getOrCreateChat(currentUserId: String, otherUserId: String): String {
//        val chatRef = db.child("chats")
//        val existingChatSnapshot = chatRef.orderByChild("userIds/$currentUserId").equalTo(true).get().await()
//
//        // Proveravamo da li već postoji chat sa tim korisnikom
//        for (snapshot in existingChatSnapshot.children) {
//            val chat = snapshot.getValue(Chat::class.java)
//            if (chat?.userIds?.contains(otherUserId) == true) {
//                return snapshot.key ?: "" // Vraćamo postojeći chat ID
//            }
//        }
//
//        // Ako ne postoji, kreiramo novi chat
//        val newChatRef = chatRef.push()
//        val receiver = getUserById(otherUserId)  // Funkcija koja uzima informacije o korisniku iz Firestore
//
//        val newChat = Chat(
//            id = newChatRef.key ?: "",
//            userIds = listOf(currentUserId, otherUserId).associateWith { true },
//            receiverFirstName = receiver.firstName,
//            receiverLastName = receiver.lastName,
//            receiverProfilePictureUrl = receiver.profilePictureUrl,
//            lastMessage = "",
//            timestamp = System.currentTimeMillis()
//        )
//        newChatRef.setValue(newChat).await()
//        return newChatRef.key ?: ""
//    }
//
//    // Funkcija za preuzimanje podataka o korisniku na osnovu ID-a
//    suspend fun getUserById(userId: String): User {
//        val document = firestore.collection("users").document(userId).get().await()
//        return document.toObject(User::class.java) ?: User()
//    }
//
//}