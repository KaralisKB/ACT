package com.example.act_mobile.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.act_mobile.ui.components.BottomNavigationBar
import com.example.act_mobile.ui.model.Stock
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun MainAppScreen(
    modifier: Modifier = Modifier,
    username: String,
    currentBalance: String,
    onAddFundsClick: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("home") }
    var selectedStock by remember { mutableStateOf<Stock?>(null) }
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    // Firestore instance
    val firestore = FirebaseFirestore.getInstance()


    Scaffold(
        bottomBar = {
            BottomNavigationBar(currentScreen = currentScreen) { selectedScreen ->
                currentScreen = selectedScreen
            }
        }
    ) { paddingValues ->
        when (currentScreen) {
            "home" -> HomeScreen(
                username = username,
                currentBalance = currentBalance,
                onAddFundsClick = { /* to do add funds functionality */ },
                modifier = modifier.padding(paddingValues)
            )
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
