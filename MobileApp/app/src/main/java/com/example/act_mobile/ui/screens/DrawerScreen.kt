package com.example.act_mobile.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.act_mobile.R

@Composable
fun DrawerScreen(
    username: String,
    profileImageUri: Uri?,
    onImageClick: () -> Unit,
    onAddFundsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onSupportClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onNewsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = if (profileImageUri != null) {
                    rememberAsyncImagePainter(profileImageUri)
                } else {
                    painterResource(id = R.drawable.ic_profile_plsceholder)
                },
                contentDescription = "Profile Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = username,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Available Funds:  ", // TO DO
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
            Button(
                onClick = onAddFundsClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .width(150.dp)
                    .height(40.dp)
            ) {
                Text(
                    text = "+ Add Funds",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }



        Spacer(modifier = Modifier.height(16.dp))

        //  menu items
        DrawerMenuItem(text = "News", onClick = onNewsClick)
        DrawerMenuItem(text = "Support & Help", onClick = onSupportClick )
        DrawerMenuItem(text = "Feedback & Reviews", onClick = onFeedbackClick )
        DrawerMenuItem(text = "Settings", onClick = onSettingsClick)
        DrawerMenuItem(text = "Log Out", onClick = onLogoutClick)
    }
}

@Composable
fun DrawerMenuItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text)
    }
}
