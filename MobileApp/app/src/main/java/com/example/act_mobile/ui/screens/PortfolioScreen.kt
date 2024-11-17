package com.example.act_mobile.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class Transaction(
    val ticker: String = "",
    val quantity: Double = 0.0,
    val price: Double = 0.0,
    val totalAmount: Double = 0.0,
    val transactionType: String = "",
    val timestamp: Timestamp? = null
)

@Composable
fun PortfolioScreen(modifier: Modifier = Modifier) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val transactions = remember { mutableStateListOf<Transaction>() }
    var totalInvested by remember { mutableStateOf(0.0) }
    var totalProfitLoss by remember { mutableStateOf(0.0) }


    LaunchedEffect(userId) {
        if (userId != null) {
            Log.d("PortfolioScreen", "Fetching transactions for user: $userId")
            fetchTransactions(userId) { fetchedTransactions, invested, profitLoss ->
                transactions.clear()
                transactions.addAll(fetchedTransactions)
                totalInvested = invested
                totalProfitLoss = profitLoss
                Log.d("PortfolioScreen", "Transactions fetched: ${transactions.size}")
            }
        } else {
            Log.e("PortfolioScreen", "User ID is null")
        }
    }

    Scaffold(
        modifier = modifier,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Transaction History", style = MaterialTheme.typography.h6, modifier = Modifier.padding(bottom = 8.dp))

            // all transactions
            if (transactions.isNotEmpty()) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(transactions) { transaction ->
                        TransactionRow(transaction)
                    }
                }
            } else {
                Text("No transactions found.", color = Color.Gray, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // summary at the bottom
            Text("Total Invested: $${String.format("%.2f", totalInvested)}", fontSize = 16.sp)
            Text(
                "Total Profit/Loss: $${String.format("%.2f", totalProfitLoss)}",
                color = if (totalProfitLoss >= 0) Color.Green else Color.Red,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val date = transaction.timestamp?.toDate()?.let { dateFormatter.format(it) } ?: "Unknown Date"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("${transaction.ticker} - ${transaction.transactionType.capitalize()}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Quantity: ${transaction.quantity}", fontSize = 12.sp)
            Text("Price: $${transaction.price}", fontSize = 12.sp)
            Text("Total: $${transaction.totalAmount}", fontSize = 12.sp)
            Text("Date: $date", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

fun fetchTransactions(userId: String, onResult: (List<Transaction>, Double, Double) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val transactionsRef = db.collection("transactions")
        .whereEqualTo("userId", userId)
        .orderBy("timestamp", Query.Direction.DESCENDING)

    transactionsRef.get()
        .addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) {
                Log.d("PortfolioScreen", "No transactions found for user: $userId")
                onResult(emptyList(), 0.0, 0.0)
            } else {
                val transactions = snapshot.documents.mapNotNull { document ->
                    document.toObject(Transaction::class.java)
                }

                // calculates total invested and profit/loss
                val totalInvested = transactions.filter { it.transactionType == "buy" }.sumOf { it.totalAmount }
                val totalProfitLoss = transactions.filter { it.transactionType == "sell" }.sumOf { it.totalAmount } - totalInvested

                onResult(transactions, totalInvested, totalProfitLoss)
                Log.d("PortfolioScreen", "Fetched ${transactions.size} transactions for user: $userId")
            }
        }
        .addOnFailureListener { e ->
            Log.e("PortfolioScreen", "Error fetching transactions: ${e.message}")
        }
}
