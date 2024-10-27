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
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onEditUsernameClick: () -> Unit,
    onChangeEmailClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
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
                onConfirm = {
                    onDeleteAccountClick()
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

fun deleteAccount(auth: FirebaseAuth, onSuccess: () -> Unit) {
    val user = auth.currentUser
    user?.delete()?.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onSuccess()
        } else {
            task.exception?.printStackTrace()
        }
    }
}

@Composable
fun ConfirmDeleteAccountDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account") },
        text = { Text("Are you sure you want to delete your account?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
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
