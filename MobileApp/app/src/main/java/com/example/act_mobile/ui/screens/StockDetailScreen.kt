package com.example.act_mobile.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.act_mobile.model.AggregateResult
import com.example.act_mobile.model.StockDetail
import com.example.act_mobile.network.fetchHistoricalData
import com.example.act_mobile.ui.network.fetchUserBalance
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StockDetailScreen(stock: StockDetail, onClose: () -> Unit) {
    val historicalData = remember { mutableStateListOf<AggregateResult>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf("2024-10-01") }
    var endDate by remember { mutableStateOf("2024-10-31") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val firstDayData = historicalData.firstOrNull()
    var selectedPrice by remember { mutableStateOf<Double?>(null) }
    var showBuyDialog by remember { mutableStateOf(false) }
    var showSellDialog by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf(1) }
    val coroutineScope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.email
    val context = LocalContext.current


    // fetch stock data when dates are updated
    LaunchedEffect(startDate, endDate) {
        fetchHistoricalData(
            ticker = stock.ticker ?: "",
            from = startDate,
            to = endDate
        ) { data, error ->
            if (data != null) {
                historicalData.clear()
                historicalData.addAll(data)
                errorMessage = null
            } else {
                errorMessage = error
            }
        }
    }

    val closePrices = historicalData.mapNotNull { it.closePrice }
    val maxPrice = closePrices.maxOrNull()
    val minPrice = closePrices.minOrNull()
    val avgPrice = closePrices.average().takeIf { closePrices.isNotEmpty() }

    // calcaultes Open, High, Low, Volume for the selected date range
    val openPrice = firstDayData?.openPrice ?: "N/A"
    val highPrice = historicalData.maxOfOrNull { it.highPrice ?: Double.MIN_VALUE }
    val lowPrice = historicalData.minOfOrNull { it.lowPrice ?: Double.MAX_VALUE }
    val totalVolume = historicalData.sumOf { it.volume ?: 0L }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${stock.ticker}", color = Color.Black) },
                backgroundColor = Color.White,
                navigationIcon = {
                    IconButton(onClick = { onClose() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = paddingValues.calculateTopPadding(), bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // logo and ticker info
                stock.branding?.iconUrl?.let { url ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("$url?apiKey=ZT6N7key2jS5_Jz4l6BrlRUIq_9Pknx6")
                            .crossfade(true)
                            .build(),
                        contentDescription = "${stock.name} Icon",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(end = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("${stock.name}", style = MaterialTheme.typography.h5)
                    Text("Current Price: ${stock.currentPrice}", style = MaterialTheme.typography.h6)
                    Text("Market Cap: ${stock.marketCap}", style = MaterialTheme.typography.body2)
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Button(
                        onClick = { showBuyDialog = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text("Buy", color = Color.White)
                    }
                    IconButton(onClick = {
                        addToWatchlist(stock)
                    }) {
                        Icon(Icons.Default.Visibility, contentDescription = "Add to Watchlist")
                    }
                }
            }

            if (showBuyDialog) {
                BuyDialog(
                    currentPrice = stock.currentPrice ?: 0.0,
                    onDismiss = { showBuyDialog = false },
                    onConfirm = { quantity ->
                        coroutineScope.launch {
                            handleBuyTransaction(quantity, stock.currentPrice ?: 0.0, stock, context)
                            showBuyDialog = false
                        }
                    }
                )
            }

            if (showSellDialog) {
                SellDialog(
                    currentPrice = stock.currentPrice ?: 0.0,
                    onDismiss = { showSellDialog = false },
                    onConfirm = { quantity ->
                        coroutineScope.launch {
                            handleSellTransaction(quantity, stock.currentPrice ?: 0.0, stock)
                            showSellDialog = false
                        }
                    }
                )
            }


            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Start Date: $startDate",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { showStartDatePicker = true }
                        .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "End Date: $endDate",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { showEndDatePicker = true }
                        .background(Color.LightGray, shape = MaterialTheme.shapes.small)
                        .padding(8.dp)
                )
            }

            if (showStartDatePicker) {
                ShowDatePickerDialog(startDate) { selectedDate ->
                    startDate = selectedDate
                    showStartDatePicker = false
                }
            }

            if (showEndDatePicker) {
                ShowDatePickerDialog(endDate) { selectedDate ->
                    endDate = selectedDate
                    showEndDatePicker = false
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            if (historicalData.isNotEmpty()) {
                LineChartView(historicalData.mapNotNull { it.closePrice }) { price ->
                    selectedPrice = price
                }
            } else if (errorMessage != null) {
                Text("Error: $errorMessage", color = Color.Red)
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Price: ${selectedPrice?.toString() ?: "N/A"}")


            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp)
            ) {
                Text("Open: $openPrice", fontWeight = FontWeight.Bold)
                Text("Intraday High: ${highPrice ?: "N/A"}")
                Text("Intraday Low: ${lowPrice ?: "N/A"}")
                Text("Volume: $totalVolume")
                Text("Max Price: ${maxPrice ?: "N/A"}", fontWeight = FontWeight.Bold)
                Text("Min Price: ${minPrice ?: "N/A"}")
                Text("Average Price: ${avgPrice?.let { String.format("%.2f", it) } ?: "N/A"}")
            }
        }
    }
}




@Composable
fun ShowDatePickerDialog(initialDate: String, onDateSelected: (String) -> Unit) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance().apply {
        time = dateFormat.parse(initialDate) ?: Date()
    }

    DatePickerDialog(
        LocalContext.current,
        { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            onDateSelected(dateFormat.format(selectedDate.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
fun LineChartView(prices: List<Double>, onPriceSelected: (Double?) -> Unit) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = {
            LineChart(context).apply {
                description = Description().apply { text = "Historical Price Data" }
                setBackgroundColor(Color.White.toArgb())
                setNoDataText("No historical data available")
                setDrawGridBackground(false)
                axisRight.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    textColor = Color.Gray.toArgb()
                    textSize = 12f
                }

                axisLeft.apply {
                    textColor = Color.Gray.toArgb()
                    textSize = 12f
                    setDrawGridLines(true)
                    gridColor = Color.LightGray.toArgb()
                }

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.y?.let { onPriceSelected(it.toDouble()) } // Pass selected price
                    }

                    override fun onNothingSelected() {
                        onPriceSelected(null)
                    }
                })
            }
        },
        update = { chart ->
            val entries = prices.mapIndexed { index, price -> Entry(index.toFloat(), price.toFloat()) }
            val dataSet = LineDataSet(entries, "Price").apply {
                color = Color.Blue.toArgb()
                valueTextColor = Color.Black.toArgb()
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

fun addToWatchlist(stock: StockDetail) {
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId != null) {
        val watchlistRef = db.collection("users").document(userId).collection("watchlist")
        val watchlistData = hashMapOf(
            "ticker" to stock.ticker,
            "name" to stock.name,
            "currentPrice" to stock.currentPrice,
            "primaryExchange" to stock.primaryExchange,
            "branding" to stock.branding?.iconUrl
        )

        watchlistRef.document(stock.ticker ?: "").set(watchlistData)
            .addOnSuccessListener {
                Log.d("Watchlist", "Stock added to watchlist")
            }
            .addOnFailureListener { e ->
                Log.e("Watchlist", "Error adding to watchlist: ${e.message}")
            }
    }
}


@Composable
fun BuyDialog(currentPrice: Double, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var quantity by remember { mutableStateOf(1) }
    val totalCost = quantity * currentPrice

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Buy Stock") },
        text = {
            Column {
                Text("Current Price: $currentPrice")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter Quantity:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = quantity.toString(),
                    onValueChange = { newValue ->
                        quantity = newValue.toIntOrNull() ?: 1
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Quantity") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Cost: $totalCost")
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(quantity)
            }) {
                Text("Trade")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SellDialog(currentPrice: Double, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var quantity by remember { mutableStateOf(1) }
    val totalEarnings = quantity * currentPrice

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Sell Stock") },
        text = {
            Column {
                Text("Current Price: $currentPrice")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Enter Quantity to Sell:")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = quantity.toString(),
                    onValueChange = { newValue ->
                        quantity = newValue.toIntOrNull() ?: 1
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Quantity") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Total Earnings: $totalEarnings")
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(quantity)
            }) {
                Text("Sell")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

fun handleBuyTransaction(quantity: Int, currentPrice: Double, stock: StockDetail, context: Context) {
    val db = Firebase.firestore
    val totalCost = quantity * currentPrice
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId == null) {
        Log.e("Transaction", "User ID is null. Cannot proceed with transaction.")
        return
    }

    val userRef = db.collection("users").document(userId)
    val holdingsRef = userRef.collection("holdings")
    val transactionsRef = db.collection("transactions")

    // checks  the number of stocks in the user's portfolio
    holdingsRef.get().addOnSuccessListener { holdingsSnapshot ->
        if (holdingsSnapshot.size() >= 13) {
            // Display a message to the user
            Log.e("Transaction", "Maximum portfolio limit reached. Cannot add more stocks.")
            Toast.makeText(context, "Cannot hold more than 13 stocks in your portfolio.", Toast.LENGTH_LONG).show()
            return@addOnSuccessListener
        }

        // prceedes with transaction if the portfolio limit is not exceeded
        userRef.get().addOnSuccessListener { document ->
            val currentBalance = document.getDouble("balance") ?: 0.0

            if (currentBalance < totalCost) {
                Log.e("Transaction", "Insufficient balance")
                Toast.makeText(context, "Insufficient balance to complete this purchase.", Toast.LENGTH_LONG).show()
                return@addOnSuccessListener
            }

            // minus cost from balance
            userRef.update("balance", FieldValue.increment(-totalCost))
                .addOnSuccessListener {
                    val newHolding = hashMapOf(
                        "ticker" to stock.ticker,
                        "quantity" to quantity,
                        "investedPrice" to currentPrice,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                    holdingsRef.add(newHolding)
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
                    transactionsRef.add(transactionData)
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
        }.addOnFailureListener { e ->
            Log.e("Transaction", "Error retrieving user document: ${e.message}")
        }
    }.addOnFailureListener { e ->
        Log.e("Transaction", "Error retrieving holdings: ${e.message}")
    }
}

//
//fun handleBuyTransaction(quantity: Int, currentPrice: Double, stock: StockDetail) {
//    val db = FirebaseFirestore.getInstance()
//    val totalCost = quantity * currentPrice
//    val userId = FirebaseAuth.getInstance().currentUser?.uid
//
//    if (userId == null) {
//        Log.e("Transaction", "User ID is null. Cannot proceed with transaction.")
//        return
//    }
//
//    val userRef = db.collection("users").document(userId)
//    val holdingsRef = userRef.collection("holdings")
//    val transactionsRef = db.collection("transactions")
//
//    holdingsRef.get().addOnSuccessListener { holdingsSnapshot ->
//        if (holdingsSnapshot.size() >= 13) {
//            Log.e("Transaction", "Maximum portfolio limit reached. Cannot add more stocks.")
//            return@addOnSuccessListener
//        }
//
//        userRef.get().addOnSuccessListener { document ->
//            val currentBalance = document.getDouble("balance") ?: 0.0
//
//            if (currentBalance < totalCost) {
//                Log.e("Transaction", "Insufficient balance")
//                return@addOnSuccessListener
//            }
//
//            userRef.update("balance", FieldValue.increment(-totalCost))
//                .addOnSuccessListener {
//                    val newHolding = hashMapOf(
//                        "ticker" to stock.ticker,
//                        "quantity" to quantity,
//                        "investedPrice" to currentPrice,
//                        "timestamp" to FieldValue.serverTimestamp()
//                    )
//                    holdingsRef.add(newHolding)
//                        .addOnSuccessListener {
//                            Log.d("Transaction", "New holding entry added successfully")
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e("Transaction", "Error adding holding entry: ${e.message}")
//                        }
//
//                    val transactionData = hashMapOf(
//                        "userId" to userId,
//                        "ticker" to stock.ticker,
//                        "transactionType" to "buy",
//                        "quantity" to quantity,
//                        "price" to currentPrice,
//                        "totalAmount" to totalCost,
//                        "timestamp" to FieldValue.serverTimestamp()
//                    )
//                    transactionsRef.add(transactionData)
//                        .addOnSuccessListener {
//                            Log.d("Transaction", "Buy transaction saved successfully")
//                        }
//                        .addOnFailureListener { e ->
//                            Log.e("Transaction", "Error saving buy transaction: ${e.message}")
//                        }
//                }
//                .addOnFailureListener { e ->
//                    Log.e("Transaction", "Error updating balance: ${e.message}")
//                }
//        }.addOnFailureListener { e ->
//            Log.e("Transaction", "Error retrieving user document: ${e.message}")
//        }
//    }.addOnFailureListener { e ->
//        Log.e("Transaction", "Error retrieving holdings: ${e.message}")
//    }
//}


fun handleSellTransaction(quantity: Int, currentPrice: Double, stock: StockDetail) {
    val db = FirebaseFirestore.getInstance()
    val totalEarnings = quantity * currentPrice
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    if (userId == null) {
        Log.e("Transaction", "User ID is null. Cannot proceed with transaction.")
        return
    }

    val userRef = db.collection("users").document(userId)
    val holdingsRef = userRef.collection("holdings").document(stock.ticker ?: "unknown")

    holdingsRef.get().addOnSuccessListener { holdingsSnapshot ->
        val currentHoldings = holdingsSnapshot.getDouble("quantity") ?: 0.0

        if (currentHoldings < quantity) {
            Log.e("Transaction", "Insufficient holdings: Current Holdings = $currentHoldings, Required = $quantity")
            return@addOnSuccessListener
        }

        if (currentHoldings == quantity.toDouble()) {
            holdingsRef.delete()
        } else {
            holdingsRef.update("quantity", FieldValue.increment(-quantity.toDouble()))
        }

        userRef.update("balance", FieldValue.increment(totalEarnings))
            .addOnSuccessListener {
                val transactionData = hashMapOf(
                    "userId" to userId,
                    "ticker" to stock.ticker,
                    "transactionType" to "sell",
                    "quantity" to quantity,
                    "price" to currentPrice,
                    "totalAmount" to totalEarnings,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                db.collection("transactions").add(transactionData)
                    .addOnSuccessListener {
                        Log.d("Transaction", "Sell transaction saved successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Transaction", "Error saving sell transaction: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Transaction", "Error updating balance: ${e.message}")
            }
    }.addOnFailureListener { e ->
        Log.e("Transaction", "Error retrieving holdings: ${e.message}")
    }
}

//
//import android.app.DatePickerDialog
//import android.util.Log
//import androidx.activity.compose.BackHandler
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.toArgb
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.viewinterop.AndroidView
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//import com.example.act_mobile.model.AggregateResult
//import com.example.act_mobile.model.StockDetail
//import com.example.act_mobile.network.fetchHistoricalData
//import com.github.mikephil.charting.charts.LineChart
//import com.github.mikephil.charting.components.Description
//import com.github.mikephil.charting.components.XAxis
//import com.github.mikephil.charting.data.Entry
//import com.github.mikephil.charting.data.LineData
//import com.github.mikephil.charting.data.LineDataSet
//import com.github.mikephil.charting.highlight.Highlight
//import com.github.mikephil.charting.listener.OnChartValueSelectedListener
//import kotlinx.coroutines.launch
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//
//@Composable
//fun StockDetailScreen(stock: StockDetail, onClose: () -> Unit) {
//    val historicalData = remember { mutableStateListOf<AggregateResult>() }
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//    var startDate by remember { mutableStateOf("2023-01-01") }
//    var endDate by remember { mutableStateOf("2023-12-31") }
//    var showStartDatePicker by remember { mutableStateOf(false) }
//    var showEndDatePicker by remember { mutableStateOf(false) }
//    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//    val firstDayData = historicalData.firstOrNull()
//    var selectedPrice by remember { mutableStateOf<Double?>(null) }
//    var showBuyDialog by remember { mutableStateOf(false) }
//    var quantity by remember { mutableStateOf(1) }
//
//
//
//
//    // Fetch data when dates are updated
//    LaunchedEffect(startDate, endDate) {
//        fetchHistoricalData(
//            ticker = stock.ticker ?: "",
//            from = startDate,
//            to = endDate
//        ) { data, error ->
//            if (data != null) {
//                historicalData.clear()
//                historicalData.addAll(data)
//                errorMessage = null
//            } else {
//                errorMessage = error
//            }
//        }
//    }
//
//    val closePrices = historicalData.mapNotNull { it.closePrice }
//    val maxPrice = closePrices.maxOrNull()
//    val minPrice = closePrices.minOrNull()
//    val avgPrice = closePrices.average().takeIf { closePrices.isNotEmpty() }
//
//    // Calculate Open, High, Low, Volume for the selected date range
//    val openPrice = firstDayData?.openPrice ?: "N/A"
//    val highPrice = historicalData.maxOfOrNull { it.highPrice ?: Double.MIN_VALUE }
//    val lowPrice = historicalData.minOfOrNull { it.lowPrice ?: Double.MAX_VALUE }
//    val totalVolume = historicalData.sumOf { it.volume ?: 0L }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("${stock.ticker}", color = Color.Black) },
//                backgroundColor = Color.White,
//                navigationIcon = {
//                    IconButton(onClick = { onClose() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .padding(horizontal = 16.dp) // Remove vertical padding to minimize space
//                .padding(top = paddingValues.calculateTopPadding(), bottom = 16.dp)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 4.dp), // Adjusted vertical padding here
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Logo and Ticker Details
//                stock.branding?.iconUrl?.let { url ->
//                    AsyncImage(
//                        model = ImageRequest.Builder(LocalContext.current)
//                            .data("$url?apiKey=ZT6N7key2jS5_Jz4l6BrlRUIq_9Pknx6")
//                            .crossfade(true)
//                            .build(),
//                        contentDescription = "${stock.name} Icon",
//                        modifier = Modifier
//                            .size(48.dp)
//                            .padding(end = 8.dp),
//                        contentScale = ContentScale.Fit
//                    )
//                }
//                Spacer(modifier = Modifier.width(8.dp))
//                Column {
//                    Text("${stock.name}", style = MaterialTheme.typography.h5)
//                    Text("Current Price: ${stock.currentPrice}", style = MaterialTheme.typography.h6)
//                    Text("Market Cap: ${stock.marketCap}", style = MaterialTheme.typography.body2)
//                }
//                Spacer(modifier = Modifier.weight(1f))
//                Column(horizontalAlignment = Alignment.End) {
//                    Button(
//                        onClick = { /* Handle Buy */ },
//                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    ) {
//                        Text("Buy", color = Color.White)
//                    }
//                    Button(
//                        onClick = { /* Handle Sell */ },
//                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF44336)),
//                        modifier = Modifier.padding(vertical = 4.dp)
//                    ) {
//                        Text("Sell", color = Color.White)
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = "Start Date: $startDate",
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier
//                        .clickable { showStartDatePicker = true }
//                        .background(Color.LightGray, shape = MaterialTheme.shapes.small)
//                        .padding(8.dp)
//                )
//                Spacer(modifier = Modifier.width(16.dp))
//                Text(
//                    text = "End Date: $endDate",
//                    fontWeight = FontWeight.Bold,
//                    modifier = Modifier
//                        .clickable { showEndDatePicker = true }
//                        .background(Color.LightGray, shape = MaterialTheme.shapes.small)
//                        .padding(8.dp)
//                )
//            }
//
//            if (showStartDatePicker) {
//                ShowDatePickerDialog(startDate) { selectedDate ->
//                    startDate = selectedDate
//                    showStartDatePicker = false
//                }
//            }
//
//            if (showEndDatePicker) {
//                ShowDatePickerDialog(endDate) { selectedDate ->
//                    endDate = selectedDate
//                    showEndDatePicker = false
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//
//            if (historicalData.isNotEmpty()) {
//                LineChartView(historicalData.mapNotNull { it.closePrice }) { price ->
//                    selectedPrice = price
//                }
//            } else if (errorMessage != null) {
//                Text("Error: $errorMessage", color = Color.Red)
//            } else {
//                Box(modifier = Modifier.fillMaxWidth()) {
//                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Text("Price: ${selectedPrice?.toString() ?: "N/A"}")
//
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            Column(
//                modifier = Modifier
//                    .background(Color(0xFFF5F5F5))
//                    .padding(12.dp)
//            ) {
//                Text("Open: $openPrice", fontWeight = FontWeight.Bold)
//                Text("Intraday High: ${highPrice ?: "N/A"}")
//                Text("Intraday Low: ${lowPrice ?: "N/A"}")
//                Text("Volume: $totalVolume")
//                Text("Max Price: ${maxPrice ?: "N/A"}", fontWeight = FontWeight.Bold)
//                Text("Min Price: ${minPrice ?: "N/A"}")
//                Text("Average Price: ${avgPrice?.let { String.format("%.2f", it) } ?: "N/A"}")
//            }
//        }
//    }
//}
//
//
//
//
//@Composable
//fun ShowDatePickerDialog(initialDate: String, onDateSelected: (String) -> Unit) {
//    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//    val calendar = Calendar.getInstance().apply {
//        time = dateFormat.parse(initialDate) ?: Date()
//    }
//
//    DatePickerDialog(
//        LocalContext.current,
//        { _, year, month, dayOfMonth ->
//            val selectedDate = Calendar.getInstance().apply {
//                set(year, month, dayOfMonth)
//            }
//            onDateSelected(dateFormat.format(selectedDate.time))
//        },
//        calendar.get(Calendar.YEAR),
//        calendar.get(Calendar.MONTH),
//        calendar.get(Calendar.DAY_OF_MONTH)
//    ).show()
//}
//
//@Composable
//fun LineChartView(prices: List<Double>, onPriceSelected: (Double?) -> Unit) {
//    val context = LocalContext.current
//    AndroidView(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(300.dp),
//        factory = {
//            LineChart(context).apply {
//                description = Description().apply { text = "Historical Price Data" }
//                setBackgroundColor(Color.White.toArgb())
//                setNoDataText("No historical data available")
//                setDrawGridBackground(false)
//                axisRight.isEnabled = false
//
//                xAxis.apply {
//                    position = XAxis.XAxisPosition.BOTTOM
//                    setDrawGridLines(false)
//                    textColor = Color.Gray.toArgb()
//                    textSize = 12f
//                }
//
//                axisLeft.apply {
//                    textColor = Color.Gray.toArgb()
//                    textSize = 12f
//                    setDrawGridLines(true)
//                    gridColor = Color.LightGray.toArgb()
//                }
//
//                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//                    override fun onValueSelected(e: Entry?, h: Highlight?) {
//                        e?.y?.let { onPriceSelected(it.toDouble()) } // Pass selected price
//                    }
//
//                    override fun onNothingSelected() {
//                        onPriceSelected(null)
//                    }
//                })
//            }
//        },
//        update = { chart ->
//            val entries = prices.mapIndexed { index, price -> Entry(index.toFloat(), price.toFloat()) }
//            val dataSet = LineDataSet(entries, "Price").apply {
//                color = Color.Blue.toArgb()
//                valueTextColor = Color.Black.toArgb()
//                lineWidth = 2f
//                setDrawCircles(false)
//                setDrawValues(false)
//                mode = LineDataSet.Mode.CUBIC_BEZIER
//            }
//            chart.data = LineData(dataSet)
//            chart.invalidate()
//        }
//    )
//}
