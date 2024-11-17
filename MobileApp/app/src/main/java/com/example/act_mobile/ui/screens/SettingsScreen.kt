package com.example.act_mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onEditUsernameClick: () -> Unit,
    onChangeEmailClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: (String, String) -> Unit,
    onNotificationsClick: () -> Unit,
    onPrivacyPreferencesClick: () -> Unit,
    onAppearanceClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // user settings section
            Text(text = "Account Settings", modifier = Modifier.padding(vertical = 8.dp))
            Divider()
            SettingsItem(text = "Edit Username", onClick = onEditUsernameClick)
            SettingsItem(text = "Change Email", onClick = onChangeEmailClick)
            SettingsItem(text = "Change Password", onClick = onChangePasswordClick)
            SettingsItem(text = "Delete Account", onClick = { showDialog = true })

            Spacer(modifier = Modifier.height(24.dp))

            // app settings section
            Text(text = "App Settings", modifier = Modifier.padding(vertical = 8.dp))
            Divider()
            SettingsItem(text = "Notifications", onClick = onNotificationsClick)
            SettingsItem(text = "Privacy Preferences", onClick = onPrivacyPreferencesClick)
            SettingsItem(text = "Appearance (Dark/Light Mode)", onClick = onAppearanceClick)
        }

        if (showDialog) {
            ConfirmDeleteAccountDialog(
                onConfirm = { email, password ->
                    onDeleteAccountClick(email, password)
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun SettingsItem(text: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(text = text)
    }
}

fun deleteAccount(
    auth: FirebaseAuth,
    email: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    val user = auth.currentUser
    val credential = EmailAuthProvider.getCredential(email, password)

    user?.reauthenticate(credential)?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            user.delete().addOnCompleteListener { deleteTask ->
                if (deleteTask.isSuccessful) {
                    val userId = user.uid
                    val firestore = FirebaseFirestore.getInstance()

                    firestore.collection("users").document(userId).delete()
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            onError(exception)
                        }
                } else {
                    deleteTask.exception?.let { onError(it) }
                }
            }
        } else {
            task.exception?.let { onError(it) }
        }
    }
}


@Composable
fun ConfirmDeleteAccountDialog(
    onConfirm: (String, String) -> Unit,  // email, password
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account") },
        text = {
            Column {
                Text("Please enter your email and password to confirm deletion.")
                TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                TextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(email, password) }) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
