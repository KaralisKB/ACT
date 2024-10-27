package com.example.act_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onLoginClick: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    // firebase and firestore auth instance
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    Scaffold(
        scaffoldState = scaffoldState
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // username field
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // emamil field
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // pw field
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    loading = true
                    coroutineScope.launch {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                loading = false
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid

                                    // storing username and email in firestore
                                    if (userId != null) {
                                        val user = hashMapOf(
                                            "username" to username,
                                            "email" to email
                                        )

                                        firestore.collection("users").document(userId).set(user)
                                            .addOnSuccessListener {
                                                Log.d("RegisterScreen", "User data successfully for: $username")
                                                onRegisterSuccess() // navigates to login screen after register
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("RegisterScreen", "Error ", e)
                                                coroutineScope.launch {
                                                    scaffoldState.snackbarHostState.showSnackbar("Failed to save user data: ${e.message}")
                                                }
                                            }
                                    }
                                } else {
                                    val errorMessage = task.exception?.message ?: "Unknown error"
                                    Log.e("RegisterScreen", "Registration failed: $errorMessage")
                                    coroutineScope.launch {
                                        scaffoldState.snackbarHostState.showSnackbar("Registration failed: $errorMessage")
                                    }
                                }
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Register")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onLoginClick) {
                Text("Already have an account? Log in")
            }

        }
    }
}
