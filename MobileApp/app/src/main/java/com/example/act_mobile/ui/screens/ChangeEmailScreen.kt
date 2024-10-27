package com.example.act_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChangeEmailScreen(
    currentEmail: String,
    onBackClick: () -> Unit,
    onSaveClick: (String) -> Unit
) {
    var newEmail by remember { mutableStateOf(currentEmail) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Email") },
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

            // input for new email
            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("New Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSaveClick(newEmail) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

fun updateEmail(auth: FirebaseAuth, newEmail: String, onSuccess: () -> Unit) {
    val user = auth.currentUser
    user?.updateEmail(newEmail)?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onSuccess()
        } else {
            task.exception?.printStackTrace()
        }
    }
}
