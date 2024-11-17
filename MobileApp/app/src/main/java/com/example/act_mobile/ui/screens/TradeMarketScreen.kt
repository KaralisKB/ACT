package com.example.act_mobile.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.act_mobile.model.StockDetail
import com.example.act_mobile.network.fetchPreviousClose
import com.example.act_mobile.network.fetchStockDetails
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TradeMarketScreen(
    modifier: Modifier = Modifier,
    onStockClick: (StockDetail) -> Unit,
    onSearchClick: () -> Unit,
    onWatchlistClick: () -> Unit
) {
    var stockList by remember { mutableStateOf<List<StockDetail>>(emptyList()) }
    var displayedList by remember { mutableStateOf<List<StockDetail>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSearchVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val tickers = listOf("AAPL", "AMZN") // can add more tickers if needed
        val fetchedStocks = mutableListOf<StockDetail>()

        for (ticker in tickers) {
            coroutineScope.launch {
                fetchStockDetails(ticker) { stock, error ->
                    if (stock != null) {
                        fetchPreviousClose(ticker) { price, _ ->
                            stock.currentPrice = price
                            fetchedStocks.add(stock)
                            stockList = fetchedStocks.toList()
                            displayedList = stockList
                        }
                    } else {
                        errorMessage = error
                    }
                }
            }
            delay(1000L)
        }

        stockList = fetchedStocks
        displayedList = stockList
        isLoading = false
    }

    Column(modifier = modifier.fillMaxSize()) {
        // display search bar if search is active
        if (isSearchVisible) {
            TextField(
                value = searchText,
                onValueChange = { newValue ->
                    searchText = newValue
                    displayedList = stockList.filter {
                        it.name?.contains(newValue, ignoreCase = true) == true ||
                                it.ticker?.contains(newValue, ignoreCase = true) == true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search stocks...") },
                singleLine = true
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                isSearchVisible = !isSearchVisible
                if (!isSearchVisible) {
                    searchText = ""
                    displayedList = stockList
                }
                onSearchClick()
            }) {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search Stocks")
            }
            IconButton(onClick = onWatchlistClick) {
                Icon(imageVector = Icons.Default.Visibility, contentDescription = "Watchlist")
            }
        }

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage ?: "An error occurred")
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(displayedList) { stock ->
                        StockItemCard(stock) {
                            onStockClick(stock)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun StockItemCard(stock: StockDetail, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stock.name ?: "N/A",
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.onSurface
                )
                Text(
                    text = "${stock.primaryExchange ?: "N/A"}: ${stock.ticker ?: "N/A"}",
                    style = MaterialTheme.typography.subtitle1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                )

                Divider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f),
                    thickness = 1.dp
                )
                Text(
                    text = "Current Price: ${stock.currentPrice?.let { "$$it" } ?: "N/A"}",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colors.primary
                )
            }

            stock.branding?.iconUrl?.let { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("$url?apiKey=ZT6N7key2jS5_Jz4l6BrlRUIq_9Pknx6")
                        .crossfade(true)
                        .build(),
                    contentDescription = "${stock.name} Icon",
                    modifier = Modifier
                        .size(58.dp)
                        .padding(start = 8.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

//        val tickers = listOf("AAPL", "MSFT")
////            "GOOG", "AMZN", "BRK.A", "TSLA", "UNH", "JNJ", "XOM", "V",
////            "NVDA", "WMTN", "JPM.PRC", "PG", "LLY", "CVX", "MA", "HD", "FB", "BAC",
////            "ABBV", "PFE", "MRK", "KO", "PEP", "AVGO", "ORCL", "TMO", "COST", "CSCO",
////            "MCD", "DHR", "ACN", "TMUS", "ABT", "DIS", "NKE", "WFC", "BMY", "NEE",
////            "LIN", "UPS", "PM", "TXN", "VZ", "ADBE", "MS", "CMCSA", "SCHW", "AMGN",
////            "COP", "RTX", "HON", "CRM", "NFLX", "QCOM", "T", "CVS", "DE", "IBM")
