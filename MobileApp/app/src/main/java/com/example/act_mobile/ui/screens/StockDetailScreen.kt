package com.example.act_mobile.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.act_mobile.ui.model.Stock

@Composable
fun StockDetailScreen(stock: Stock, onBackClick: () -> Unit) {

    BackHandler(onBack = { onBackClick() })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stock.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Stock Symbol: ${stock.symbol}")
            Text("Sector: ${stock.sector}")
            Text("Market Cap: ${stock.marketCap}")
            Text("Price: ${stock.price}")

            Spacer(modifier = Modifier.height(24.dp))

            // TODO
            Text("Graphs and Trend Analysis (To be implemented)")
        }
    }
}
