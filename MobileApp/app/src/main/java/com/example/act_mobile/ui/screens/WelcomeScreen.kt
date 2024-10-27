package com.example.act_mobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(onLoginClick: () -> Unit, onRegisterClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("ACT") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome to ACT", style = MaterialTheme.typography.h4)
            Text(text = "The Future of Trading", style = MaterialTheme.typography.body1)


            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }
        }
    }
}
