package com.example.act.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.act.ui.screens.*

@Composable
fun MainNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route
    ) {
        composable(BottomNavItem.Home.route) { HomeScreen() }
        composable(BottomNavItem.Portfolio.route) { PortfolioScreen() }
        composable(BottomNavItem.Market.route) { MarketScreen() }
        composable(BottomNavItem.Trade.route) { TradeScreen() }
        composable(BottomNavItem.Notifications.route) { NotificationsScreen() }
    }
}
