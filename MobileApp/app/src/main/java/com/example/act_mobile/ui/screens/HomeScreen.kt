package com.example.act_mobile.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.act_mobile.network.fetchPreviousClose
import com.example.act_mobile.ui.model.StockHolding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    userId: String,
    username: String,
    currentBalance: String,
    modifier: Modifier = Modifier
) {
    var currentBalance by remember { mutableStateOf("Loading...") }
    var holdings by remember { mutableStateOf(listOf<StockHolding>()) }
    val coroutineScope = rememberCoroutineScope()

    var showBuySellPopup by remember { mutableStateOf(false) }
    var selectedStock: StockHolding? by remember { mutableStateOf(null) }
    var showBuyDialog by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }

    // users holdings and balance from firebase
    LaunchedEffect(userId) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId)
            .collection("holdings")
            .get()
            .addOnSuccessListener { snapshot ->
                holdings = snapshot.documents.map { document ->
                    val ticker = document.getString("ticker") ?: ""
                    val quantity = document.getDouble("quantity") ?: 0.0
                    val investedPrice = document.getDouble("investedPrice") ?: 0.0
                    val documentId = document.id
                    var currentPrice = 0.0

                    coroutineScope.launch {
                        getLatestPrice(ticker) { price ->
                            currentPrice = price ?: 0.0
                            holdings = holdings.map { holding ->
                                if (holding.ticker == ticker) holding.copy(currentPrice = currentPrice) else holding
                            }
                        }
                    }

                    StockHolding(
                        documentId = documentId,
                        ticker = ticker,
                        quantity = quantity,
                        investedPrice = investedPrice,
                        currentPrice = currentPrice
                    )
                }
            }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                currentBalance = document.getDouble("balance")?.toString() ?: "0.00"
            }
            .addOnFailureListener {
                currentBalance = "Error loading balance"
            }
    }

    val investedAmount = holdings.sumOf { it.investedPrice * it.quantity }
    val currentAmount = holdings.sumOf { it.currentPrice * it.quantity }
    val profitOrLoss = currentAmount - investedAmount

    Scaffold(modifier = modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome back, $username", style = MaterialTheme.typography.h5)
            Text("Funds Administrator", color = Color.Gray, style = MaterialTheme.typography.subtitle1)
            Text("Current Balance: $${String.format("%.2f", currentBalance.toDoubleOrNull() ?: 0.0)}", style = MaterialTheme.typography.subtitle1)

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Portfolio Summary", style = MaterialTheme.typography.h6)
                    Text("Invested Amount: $${investedAmount}")
                    Text("Current Amount: $${currentAmount}")
                    Text(
                        text = "Profit/Loss: $${String.format("%.2f", profitOrLoss)}",
                        color = if (profitOrLoss > 0) Color.Green else if (profitOrLoss < 0) Color.Red else Color.Blue
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (holdings.isEmpty()) {
                Text("No bought stocks yet", color = Color.Gray)
                Text("Explore the market and start investing!", color = MaterialTheme.colors.primary)
            } else {
                Text("Your Current Stocks", style = MaterialTheme.typography.h6)
                holdings.forEach { holding ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedStock = holding
                                showBuySellPopup = true
                            },
                        elevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text("${holding.ticker}: ${holding.quantity} shares", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Invested Price: $${holding.investedPrice}", fontSize = 12.sp, color = Color.Gray)
                                    Text("Current Price: $${holding.currentPrice}", fontSize = 12.sp, color = Color.Gray)
                                }
                                Text(
                                    text = "Profit/Loss: $${String.format("%.2f", (holding.currentPrice - holding.investedPrice) * holding.quantity)}",
                                    color = when {
                                        (holding.currentPrice - holding.investedPrice) > 0 -> Color.Green
                                        (holding.currentPrice - holding.investedPrice) < 0 -> Color.Red
                                        else -> Color.Blue
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (showBuySellPopup && selectedStock != null) {
                AlertDialog(
                    onDismissRequest = { showBuySellPopup = false },
                    title = { Text("Choose an action for ${selectedStock?.ticker}") },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = {
                                        showBuySellPopup = false
                                        showBuyDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                                ) {
                                    Text("Buy", fontSize = 12.sp, color = Color.White)
                                }
                                Button(
                                    onClick = {
                                        showBuySellPopup = false
                                        showSellDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF44336))
                                ) {
                                    Text("Sell", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showBuySellPopup = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Buy Dialog
            if (showBuyDialog && selectedStock != null) {
                BuySellDialog(
                    title = "Buy ${selectedStock?.ticker}",
                    currentPrice = selectedStock?.currentPrice ?: 0.0,
                    onDismiss = { showBuyDialog = false },
                    onConfirm = { quantity ->
                        coroutineScope.launch {
                            handleBuyTransaction(quantity, selectedStock?.currentPrice ?: 0.0, selectedStock!!)
                            showBuyDialog = false
                        }
                    }
                )
            }

            // Sell Dialog
            if (showSellDialog && selectedStock != null) {
                BuySellDialog(
                    title = "Sell ${selectedStock?.ticker}",
                    currentPrice = selectedStock?.currentPrice ?: 0.0,
                    onDismiss = { showSellDialog = false },
                    onConfirm = { quantity ->
                        coroutineScope.launch {
                            handleSellTransaction(selectedStock!!.documentId, quantity, selectedStock?.currentPrice ?: 0.0)
                            showSellDialog = false
                        }
                    }
                )
            }
        }
    }
}

// handleBuyTransaction to create a new document in holdings
fun handleBuyTransaction(quantity: Int, currentPrice: Double, stock: StockHolding) {
    val db = FirebaseFirestore.getInstance()
    val totalCost = quantity * currentPrice
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId == null) {
        Log.e("Transaction", "User ID is null. Cannot proceed with transaction.")
        return
    }

    val userRef = db.collection("users").document(userId)

    // minus cost from balance
    userRef.update("balance", FieldValue.increment(-totalCost))
        .addOnSuccessListener {
            // creates a new document for each buy in holdings
            val newHolding = hashMapOf(
                "ticker" to stock.ticker,
                "quantity" to quantity,
                "investedPrice" to currentPrice,
                "timestamp" to FieldValue.serverTimestamp()
            )
            userRef.collection("holdings").add(newHolding)
                .addOnSuccessListener {
                    Log.d("Transaction", "New holding entry added successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Transaction", "Error adding holding entry: ${e.message}")
                }

            val transactionData = hashMapOf(
                "userId" to userId,
                "ticker" to stock.ticker,
                "transactionType" to "buy",
                "quantity" to quantity,
                "price" to currentPrice,
                "totalAmount" to totalCost,
                "timestamp" to FieldValue.serverTimestamp()
            )
            // for debugging
            db.collection("transactions").add(transactionData)
                .addOnSuccessListener {
                    Log.d("Transaction", "Buy transaction saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("Transaction", "Error saving buy transaction: ${e.message}")
                }
        }
        .addOnFailureListener { e ->
            Log.e("Transaction", "Error updating balance: ${e.message}")
        }
}

// handleSellTransaction to sell a specific holding
fun handleSellTransaction(documentId: String, quantityToSell: Int, currentPrice: Double) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    val userRef = db.collection("users").document(userId)
    val holdingRef = userRef.collection("holdings").document(documentId)

    holdingRef.get().addOnSuccessListener { document ->
        val currentQuantity = document.getDouble("quantity") ?: 0.0
        val investedPrice = document.getDouble("investedPrice") ?: 0.0

        if (currentQuantity < quantityToSell) {
            Log.e("Transaction", "Insufficient quantity to sell: Available = $currentQuantity, Required = $quantityToSell")
            return@addOnSuccessListener
        }

        // calculations for profit/loss
        val totalEarnings = quantityToSell * currentPrice
        val totalInvestedAmount = quantityToSell * investedPrice
        val profitOrLoss = totalEarnings - totalInvestedAmount

        // updates or deletes the holding
        if (currentQuantity == quantityToSell.toDouble()) {
            holdingRef.delete() // if fully sold deletes the document
        } else {
            holdingRef.update("quantity", FieldValue.increment(-quantityToSell.toDouble()))
        }

        userRef.update("balance", FieldValue.increment(totalEarnings))
            .addOnSuccessListener {
                val sellTransactionData = hashMapOf(
                    "userId" to userId,
                    "ticker" to document.getString("ticker"),
                    "transactionType" to "sell",
                    "quantity" to quantityToSell,
                    "price" to currentPrice,
                    "totalAmount" to totalEarnings,
                    "profitOrLoss" to profitOrLoss,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                db.collection("transactions").add(sellTransactionData)
                    .addOnSuccessListener {
                        Log.d("Transaction", "Sell transaction logged successfully.")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Transaction", "Error logging sell transaction: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Transaction", "Error updating balance: ${e.message}")
            }
    }.addOnFailureListener { e ->
        Log.e("Transaction", "Error retrieving holding document: ${e.message}")
    }
}

suspend fun getLatestPrice(ticker: String, onPriceFetched: (Double?) -> Unit) {
    fetchPreviousClose(ticker) { price, error ->
        if (error == null && price != null) {
            onPriceFetched(price)
        } else {
            Log.e("PriceFetch", "Error fetching price for $ticker: $error")
            onPriceFetched(null)
        }
    }
}

@Composable
fun BuySellDialog(title: String, currentPrice: Double, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var quantity by remember { mutableStateOf(1) }
    val totalCost = quantity * currentPrice

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(title) },
        text = {
            Column {
                Text("Current Price: $currentPrice")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter Quantity:")
                TextField(
                    value = quantity.toString(),
                    onValueChange = { newValue -> quantity = newValue.toIntOrNull() ?: 1 },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Total: $totalCost")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(quantity) }) { Text("Confirm") }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) { Text("Cancel") }
        }
    )
}
