package com.example.act_mobile.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.act_mobile.ui.components.BottomNavigationBar
import com.example.act_mobile.ui.model.Stock


@Composable
fun MainAppScreen(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf("home") }
    var selectedStock by remember { mutableStateOf<Stock?>(null) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(currentScreen = currentScreen) { selectedScreen ->
                currentScreen = selectedScreen
            }
        }
    ) { paddingValues ->
        when (currentScreen) {
            "home" -> HomeScreen(modifier = modifier.padding(paddingValues))
            "trade" -> TradeMarketScreen(
                modifier = modifier.padding(paddingValues),
                onStockClick = { stock ->
                    selectedStock = stock
                    currentScreen = "stockDetail"
                }
            )
            "portfolio" -> PortfolioScreen(modifier = modifier.padding(paddingValues))
            "notifications" -> NotificationsScreen(modifier = modifier.padding(paddingValues))
            "stockDetail" -> selectedStock?.let { stock ->
                StockDetailScreen(
                    stock = stock,
                    onBackClick = {
                        currentScreen = "trade"
                    }
                )
            }
        }
    }
}
