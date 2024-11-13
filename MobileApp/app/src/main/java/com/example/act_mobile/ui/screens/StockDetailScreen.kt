package com.example.act_mobile.ui.screens

import android.app.DatePickerDialog
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun StockDetailScreen(stock: StockDetail, onClose: () -> Unit) {
    val historicalData = remember { mutableStateListOf<AggregateResult>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf("2023-01-01") }
    var endDate by remember { mutableStateOf("2023-12-31") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val firstDayData = historicalData.firstOrNull()
    var selectedPrice by remember { mutableStateOf<Double?>(null) } // Nullable selected price


    // Fetch data when dates are updated
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

    // Calculate Open, High, Low, Volume for the selected date range
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
                .padding(horizontal = 16.dp) // Remove vertical padding to minimize space
                .padding(top = paddingValues.calculateTopPadding(), bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp), // Adjusted vertical padding here
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo and Ticker Details
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
                        onClick = { /* Handle Buy */ },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text("Buy", color = Color.White)
                    }
                    Button(
                        onClick = { /* Handle Sell */ },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFF44336)),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text("Sell", color = Color.White)
                    }
                }
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
                    selectedPrice = price // Update the selected price
                }
            } else if (errorMessage != null) {
                Text("Error: $errorMessage", color = Color.Red)
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Price: ${selectedPrice?.toString() ?: "N/A"}") // Display the selected price here


//            if (closePrices.isNotEmpty()) {
//                LineChartView(closePrices)
//            } else if (errorMessage != null) {
//                Text("Error: $errorMessage", color = Color.Red)
//            } else {
//                Box(modifier = Modifier.fillMaxWidth()) {
//                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//                }
//            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display calculated details in a more organized way
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
                        onPriceSelected(null) // Clear selection
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

//@Composable
//fun LineChartView(prices: List<Double>) {
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
