package com.example.act_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ChangeUsernameScreen(
    currentUsername: String,
    onUsernameChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: (String) -> Unit
) {
    var newUsername by remember { mutableStateOf(currentUsername) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Username") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {

            //  input for new username
            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("New Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSaveClick(newUsername) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

fun updateUsername(userId: String, newUsername: String, onSuccess: () -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("users").document(userId).update("username", newUsername)
        .addOnSuccessListener {
            onSuccess()
        }
        .addOnFailureListener { e ->
            e.printStackTrace()
        }
}
