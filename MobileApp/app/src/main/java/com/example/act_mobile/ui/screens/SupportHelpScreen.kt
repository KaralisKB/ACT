package com.example.act_mobile.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SupportHelpScreen(onBackClick: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    BackHandler(onBack = { onBackClick() })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Support & Help") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // name field
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // email field
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // sub field
            TextField(
                value = subject,
                onValueChange = { subject = it },
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // msg field
            TextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6
            )
            Spacer(modifier = Modifier.height(16.dp))

            // sends to firebase
            Button(
                onClick = {
                    submitFormToDatabase(firestore, name, email, subject, message, context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // sends to email
            Button(
                onClick = {
                    sendEmail(context, name, email, subject, message)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Contact via Email")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // call supports
            Button(
                onClick = {
                    callSupport(context, "+00000000000")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Call Support")
            }
        }
    }
}

//  submitting form  from user to Firebase
fun submitFormToDatabase(
    firestore: FirebaseFirestore,
    name: String,
    email: String,
    subject: String,
    message: String,
    context: android.content.Context
) {
    if (subject.isBlank() || message.isBlank() || name.isBlank() || email.isBlank()) {
        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        return
    }

    val formData = hashMapOf(
        "name" to name,
        "email" to email,
        "subject" to subject,
        "message" to message
    )

    // adding data to Firestore
    firestore.collection("supportRequests")
        .add(formData)
        .addOnSuccessListener {
            Toast.makeText(context, "Form submitted successfully!", Toast.LENGTH_SHORT).show()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to submit form: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

//  to open the email client
fun sendEmail(context: android.content.Context, name: String, email: String, subject: String, message: String) {

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:acthelpcentre@gmail.com")
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, "Name: $name\nEmail: $email\n\nMessage:\n$message")
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Choose an Email client"))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
    }
}

// open the phone app for call support
fun callSupport(context: android.content.Context, phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phoneNumber")
    }

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No phone app found", Toast.LENGTH_SHORT).show()
    }
}
