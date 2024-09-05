package com.example.trackmyfit.home.search
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {


    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText
    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> get() = _searchResults

    private val firestore = FirebaseFirestore.getInstance()
    // State for filtered users
    private val _filteredUsers = MutableStateFlow<List<User>>(emptyList())
    val filteredUsers: StateFlow<List<User>> = _filteredUsers

    // List of all users fetched from Firestore
    private val allUsers = mutableListOf<User>()

    init {
        loadUsersFromFirestore()
    }

    private fun loadUsersFromFirestore() {
        FirebaseFirestore.getInstance()
            .collection("users")
            .orderBy("firstName") // Sort users by firstName alphabetically
            .get()
            .addOnSuccessListener { documents ->
                allUsers.clear()
                for (document in documents) {
                    val user = document.toObject(User::class.java)
                    allUsers.add(user)
                }
                _filteredUsers.value = allUsers
            }
    }

    fun onSearchTextChanged(text: String) {
        _searchText.value = text
        _filteredUsers.value = if (text.isEmpty()) {
            allUsers
        } else {
            allUsers.filter {
                it.firstName.contains(text, ignoreCase = true) ||
                        it.lastName.contains(text, ignoreCase = true)
            }
        }
    }
    fun searchUsers(query: String) {
        viewModelScope.launch {
            firestore.collection("users")
                .get()
                .addOnSuccessListener { documents ->
                    val users = documents.mapNotNull { document ->
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val profilePictureUrl = document.getString("profilePictureUrl") ?: ""
                        val id = document.id

                        if (firstName.contains(query, ignoreCase = true) || lastName.contains(query, ignoreCase = true)) {
                            User(id, firstName, lastName, profilePictureUrl)
                        } else {
                            null
                        }
                    }
                    _searchResults.value = users
                }
        }
    }

}
data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val profilePictureUrl: String = ""
)
