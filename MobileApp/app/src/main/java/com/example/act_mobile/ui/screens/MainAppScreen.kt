package com.example.act_mobile.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.act_mobile.model.StockDetail
import com.example.act_mobile.ui.model.StockQuote
import com.example.act_mobile.ui.components.BottomNavigationBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.stripe.android.paymentsheet.PaymentSheet

@Composable
fun MainAppScreen(
    modifier: Modifier = Modifier,
    username: String,
    currentBalance: String,

) {
    var currentScreen by remember { mutableStateOf("home") }
    var selectedStockDetail by remember { mutableStateOf<StockDetail?>(null) } // Track selected stock detail
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    var paymentAmount by remember { mutableStateOf<Int?>(null) } // State to hold payment amount

    // Firebase Firestore instance
    val firestore = FirebaseFirestore.getInstance()


    // Get the current user's ID (assuming FirebaseAuth is used)
    val userId = FirebaseAuth.getInstance().currentUser?.uid
        ?: "default_user_id" // Replace with appropriate logic

    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            BottomNavigationBar(currentScreen = currentScreen) { selectedScreen ->
                currentScreen = selectedScreen
                selectedStockDetail = null // Clear stock details on navigation
            }
        }
    ) { paddingValues ->
        when {
            selectedStockDetail != null -> { // Show StockDetailScreen if a stock is selected
                StockDetailScreen(
                    stock = selectedStockDetail!!,
                    onClose = { selectedStockDetail = null } // Close detail screen
                )
            }

            else -> {
                when (currentScreen) {
                    "home" -> HomeScreen(
                        userId = userId ?: "",
                        username = username,
                        currentBalance = currentBalance,
                        modifier = modifier.padding(paddingValues),
                    )

                    "trade" -> TradeMarketScreen(
                        modifier = modifier.padding(paddingValues),
                        onStockClick = { stock ->
                            selectedStockDetail = stock // Set the selected stock to navigate to detail screen
                        }
                    )

                    "portfolio" -> PortfolioScreen(modifier = modifier.padding(paddingValues))
                    "notifications" -> NotificationsScreen(modifier = modifier.padding(paddingValues))
                }
            }
        }
    }
}