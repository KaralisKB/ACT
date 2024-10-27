package com.example.act_mobile.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.act_mobile.R

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    BottomNavigation(
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Home") },
            selected = currentScreen == "home",
            onClick = { onTabSelected("home") },
            selectedContentColor = Color.Blue,
            unselectedContentColor = Color.Gray
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_market),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Market") },
            selected = currentScreen == "trade",
            onClick = { onTabSelected("trade") },
            selectedContentColor = Color.Blue,
            unselectedContentColor = Color.Gray
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_portfolio),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Portfolio") },
            selected = currentScreen == "portfolio",
            onClick = { onTabSelected("portfolio") },
            selectedContentColor = Color.Blue,
            unselectedContentColor = Color.Gray
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notifications),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Notifications") },
            selected = currentScreen == "notifications",
            onClick = { onTabSelected("notifications") },
            selectedContentColor = Color.Blue,
            unselectedContentColor = Color.Gray
        )
    }
}
