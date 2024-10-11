package com.example.act.ui.navigation

import com.example.act.R


sealed class BottomNavItem(val route: String, val icon: Int, val title: String) {
    object Home : BottomNavItem("home", R.drawable.ic_home, "Home")
    object Portfolio : BottomNavItem("portfolio", R.drawable.ic_portfolio, "Portfolio")
    object Market : BottomNavItem("market", R.drawable.ic_market, "Market")
    object Trade : BottomNavItem("trade", R.drawable.ic_trade, "Trade")
    object Notifications : BottomNavItem("notifications", R.drawable.ic_notifications, "Notifications")
}
