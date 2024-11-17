package com.example.act_mobile.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.act_mobile.model.Branding
import com.example.act_mobile.model.StockDetail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.act_mobile.R

@Composable
fun WatchlistScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onStockClick: (StockDetail) -> Unit
) {
    var watchlist by remember { mutableStateOf<List<StockDetail>>(emptyList()) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId).collection("watchlist")
                .get()
                .addOnSuccessListener { snapshot ->
                    val stocks = snapshot.documents.mapNotNull { document ->
                        val ticker = document.getString("ticker") ?: ""
                        val name = document.getString("name") ?: ""
                        val currentPrice = document.getDouble("currentPrice") ?: 0.0
                        val primaryExchange = document.getString("primaryExchange") ?: ""

                        StockDetail(
                            ticker = ticker,
                            name = name,
                            currentPrice = currentPrice,
                            primaryExchange = primaryExchange,
                        )
                    }

                    watchlist = stocks
                }
                .addOnFailureListener { e ->
                    Log.e("WatchlistScreen", "Error loading watchlist: ${e.message}")
                }
        } else {
            Log.e("WatchlistScreen", "User ID is null. Cannot load watchlist.")
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Watchlist", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (watchlist.isEmpty()) {
            Text("No stocks in your watchlist yet.")
        } else {
            LazyColumn {
                items(watchlist) { stock ->
                    StockItemCard(stock, onClick = { onStockClick(stock) }) {
                        removeFromWatchlist(stock, userId) { success ->
                            if (success) {
                                watchlist = watchlist.filter { it.ticker != stock.ticker }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockItemCard(stock: StockDetail, onClick: () -> Unit, onRemoveClick: () -> Unit) {
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

            Spacer(modifier = Modifier.width(8.dp))

            //  stock details
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
                Text(
                    text = "Current Price: ${stock.currentPrice?.let { "$$it" } ?: "N/A"}",
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colors.primary
                )
            }

            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.VisibilityOff, contentDescription = "Remove from watchlist")
            }
        }
    }
}



private fun removeFromWatchlist(stock: StockDetail, userId: String?, onResult: (Boolean) -> Unit) {
    if (userId == null) {
        onResult(false)
        return
    }

    val db = FirebaseFirestore.getInstance()
    val watchlistRef = db.collection("users").document(userId).collection("watchlist")
    watchlistRef
        .whereEqualTo("ticker", stock.ticker)
        .get()
        .addOnSuccessListener { snapshot ->
            val batch = db.batch()
            for (document in snapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit().addOnSuccessListener {
                onResult(true)
            }.addOnFailureListener {
                onResult(false)
            }
        }
        .addOnFailureListener {
            onResult(false)
        }
}
