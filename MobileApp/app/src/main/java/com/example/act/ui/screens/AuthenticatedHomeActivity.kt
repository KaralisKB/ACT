package com.example.act.ui.screens

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.act.ui.components.BottomNavBar
import com.example.act.ui.navigation.MainNavGraph

class AuthenticatedHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("AuthenticatedHomeActivity", "onCreate called")
        Toast.makeText(this, "Navigated to Home", Toast.LENGTH_SHORT).show()
        setContent {
            HomeScreen()  // Ensure that HomeScreen is rendering correctly
        }
    }

}

@Composable
fun HomeScreen() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Welcome to Home Screen") },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        // Main content with applied padding
        Box(
            modifier = Modifier
                .padding(innerPadding) // Apply the padding here
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "This is the home screen.")
        }
    }

}
