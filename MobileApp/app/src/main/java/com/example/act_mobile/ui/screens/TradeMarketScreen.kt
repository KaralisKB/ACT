
package com.example.act_mobile.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.act_mobile.R
import com.example.act_mobile.model.StockDetail
import com.example.act_mobile.network.ApiClient
import com.example.act_mobile.network.fetchPreviousClose
import com.example.act_mobile.network.fetchStockDetails
import com.example.act_mobile.ui.model.StockQuote
import com.example.act_mobile.ui.model.StockQuoteResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun TradeMarketScreen(modifier: Modifier = Modifier, onStockClick: (StockDetail) -> Unit) {
    var stockList by remember { mutableStateOf<List<StockDetail>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    var selectedStock by remember { mutableStateOf<StockDetail?>(null) }


    LaunchedEffect(Unit) {
        val tickers = listOf("AAPL", "AMZN") // Add more tickers as needed
        val fetchedStocks = mutableListOf<StockDetail>()

        for (ticker in tickers) {
            coroutineScope.launch {
                fetchStockDetails(ticker) { stock, error ->
                    if (stock != null) {
                        fetchPreviousClose(ticker) { price, error ->
                            if (price != null) {
                                stock.currentPrice = price // Set the current price
                            }
                            fetchedStocks.add(stock) // Add the stock with price to the list
                            stockList = fetchedStocks.toList() // Update state
                        }
                    } else {
                        errorMessage = error
                    }
                }
            }
            delay(1000L)
        }

        stockList = fetchedStocks
        isLoading = false
    }

    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        errorMessage != null -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMessage ?: "An error occurred")
            }
        }
        else -> {
            LazyColumn(modifier = modifier.padding(16.dp)) {
                items(stockList) { stock ->
                    StockItemCard(stock) {
                        onStockClick(stock)
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
            .padding(vertical = 8.dp, horizontal = 16.dp)
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
                        .listener(
                            onSuccess = { _, _ -> Log.d("StockItemCard", "Image loaded successfully for $url") },
                            onError = { _, result ->
                                Log.e("StockItemCard", "Image loading failed for $url: ${result.throwable?.message}")
                            }
                        )
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
