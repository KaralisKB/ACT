package com.example.act_mobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.act_mobile.network.ApiClient
import com.example.act_mobile.ui.model.Stock
import kotlinx.coroutines.launch

@Composable
fun TradeMarketScreen(modifier: Modifier = Modifier, onStockClick: (Stock) -> Unit) {
    val stockList = remember { mutableStateListOf<Stock>() }
    val coroutineScope = rememberCoroutineScope()

    // get data from API
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val stocks = ApiClient.stockApi.getStocks()
                stockList.addAll(stocks)
            } catch (e: Exception) {
                // any API errors
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        modifier = modifier,
    ) {
        if (stockList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(stockList) { stock ->
                    StockItem(stock = stock, onClick = { onStockClick(stock) })
                    Divider()
                }
            }
        }
    }
}

@Composable
fun StockItem(stock: Stock, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Text(text = stock.name, style = MaterialTheme.typography.h6)
        Text(text = "Symbol: ${stock.symbol}", style = MaterialTheme.typography.body2)
        Text(text = "Sector: ${stock.sector}", style = MaterialTheme.typography.body2)
        Text(text = "Market Cap: ${stock.marketCap}", style = MaterialTheme.typography.body2)
        Text(text = "Price: ${stock.price}", style = MaterialTheme.typography.body2)
    }
}