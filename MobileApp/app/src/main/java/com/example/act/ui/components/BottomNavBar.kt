package com.example.act.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.act.ui.navigation.BottomNavItem

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Portfolio,
        BottomNavItem.Market,
        BottomNavItem.Trade,
        BottomNavItem.Notifications
    )

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        contentColor = Color.Black, // This will help icons stand out
        modifier = Modifier.height(65.dp) // Increase height for better spacing
    ) {
        val currentDestination = navController.currentDestination?.route
        items.forEach { item ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(24.dp), // Adjust the icon size to be more consistent
                        tint = if (currentDestination == item.route) Color.Black else Color.Gray // Set active color
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 10.sp, // Adjust the text size for better balance
                        color = if (currentDestination == item.route) Color.Black else Color.Gray
                    )
                },
                selected = currentDestination == item.route,
                alwaysShowLabel = true, // Keep the labels visible
                onClick = {
                    navController.navigate(item.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

