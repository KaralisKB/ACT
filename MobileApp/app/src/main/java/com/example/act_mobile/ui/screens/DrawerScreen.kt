package com.example.act_mobile.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.act_mobile.R

@Composable
fun DrawerScreen(
    username: String,
    profileImageUri: Uri?,
    onImageClick: () -> Unit,
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

        Text(text = username)

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
