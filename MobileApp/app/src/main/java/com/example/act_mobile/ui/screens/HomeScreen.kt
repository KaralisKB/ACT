package com.example.act_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.act_mobile.ui.screens.AddFundsDialog
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun HomeScreen(
    userId: String,
    username: String,
    currentBalance: String,
    modifier: Modifier = Modifier,
)  {
    var currentBalance by remember { mutableStateOf("Loading...") }

    LaunchedEffect(userId) {
        FirebaseFirestore.getInstance().collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentBalance = document.getDouble("balance")?.toString() ?: "0.00"
                } else {
                    currentBalance = "0.00"
                }
            }
            .addOnFailureListener {
                currentBalance = "Error loading balance"
            }
    }



    Scaffold(
        modifier = modifier,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome back, $username",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Funds Administrator",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Current Balance: $currentBalance",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 8.dp)
            )


            Spacer(modifier = Modifier.height(45.dp))

            Text(
                text = "Your Current Bought Stocks",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "No bought stocks yet",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Your Current Sold Stocks",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "No sold stocks yet",
                style = MaterialTheme.typography.body1
            )
        }
    }
}