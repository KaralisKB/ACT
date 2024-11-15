package com.example.act_mobile.network

import android.util.Log
import com.example.act_mobile.model.AggregateResult
import com.example.act_mobile.model.HistoricalDataResponse
import com.example.act_mobile.model.PolygonResponse
import com.example.act_mobile.model.StockDetail
import com.example.act_mobile.ui.model.PreviousCloseResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun fetchStockDetails(ticker: String, onResult: (StockDetail?, String?) -> Unit) {
    val call = PolygonApiClient.instance.getStockDetails(ticker)

    call.enqueue(object : Callback<PolygonResponse> {
        override fun onResponse(call: Call<PolygonResponse>, response: Response<PolygonResponse>) {
            if (response.isSuccessful) {
                val stockDetail = response.body()?.results
                Log.d("StockDetails", "Fetched icon URL for ${stockDetail?.name}: ${stockDetail?.branding?.iconUrl}")
                onResult(stockDetail, null)            } else {
                val errorMessage = "Failed to fetch stock details: Code ${response.code()} - ${response.message()}"
                Log.e("StockDetails", errorMessage)
                onResult(null, errorMessage)
            }
        }

        override fun onFailure(call: Call<PolygonResponse>, t: Throwable) {
            val failureMessage = "Failed to fetch stock details: ${t.message}"
            Log.e("StockDetails", failureMessage)
            onResult(null, failureMessage)
        }
    })
}

fun fetchPreviousClose(ticker: String, onResult: (Double?, String?) -> Unit) {
    val call = PolygonApiClient.instance.getPreviousClose(ticker)

    call.enqueue(object : Callback<PreviousCloseResponse> {
        override fun onResponse(call: Call<PreviousCloseResponse>, response: Response<PreviousCloseResponse>) {
            if (response.isSuccessful) {
                val price = response.body()?.results?.firstOrNull()?.closePrice
                Log.d("PreviousClose", "Close Price for $ticker: $price") // Debugging log
                onResult(price, null)
            } else {
                val errorMessage = "Failed to fetch price: Code ${response.code()} - ${response.message()}"
                Log.e("PreviousClose", errorMessage)
                onResult(null, errorMessage)
            }
        }

        override fun onFailure(call: Call<PreviousCloseResponse>, t: Throwable) {
            val failureMessage = "Failed to fetch price: ${t.message}"
            Log.e("PreviousClose", failureMessage)
            onResult(null, failureMessage)
        }
    })
}

fun fetchHistoricalData(
    ticker: String,
    from: String,
    to: String,
    onResult: (List<AggregateResult>?, String?) -> Unit
) {
    val call = PolygonApiClient.instance.getAggregates(
        ticker = ticker,
        multiplier = 1,
        timespan = "day",
        from = from,
        to = to,
        apiKey = "ZT6N7key2jS5_Jz4l6BrlRUIq_9Pknx6"
    )
    call.enqueue(object : Callback<HistoricalDataResponse> {
        override fun onResponse(call: Call<HistoricalDataResponse>, response: Response<HistoricalDataResponse>) {
            if (response.isSuccessful) {
                val results = response.body()?.results
                onResult(results, null)
            } else {
                val errorMessage = "Failed to fetch data: ${response.message()}"
                Log.e("fetchHistoricalData", errorMessage)
                onResult(null, errorMessage)
            }
        }

        override fun onFailure(call: Call<HistoricalDataResponse>, t: Throwable) {
            val failureMessage = "Failed to fetch data: ${t.message}"
            Log.e("fetchHistoricalData", failureMessage)
            onResult(null, failureMessage)
        }
    })
}


//
//    call.enqueue(object : Callback<HistoricalDataResponse> {
//        override fun onResponse(call: Call<HistoricalDataResponse>, response: Response<HistoricalDataResponse>) {
//            if (response.isSuccessful) {
//                val prices = response.body()?.results?.map { it.closePrice } // Extracts only closing prices
//                onResult(prices, null)
//            } else {
//                val errorMessage = "Failed to fetch data: Code ${response.code()} - ${response.message()}"
//                Log.e("fetchHistoricalData", errorMessage)
//                onResult(null, errorMessage)
//            }
//        }
//
//        override fun onFailure(call: Call<HistoricalDataResponse>, t: Throwable) {
//            val failureMessage = "Failed to fetch data: ${t.message}"
//            Log.e("fetchHistoricalData", failureMessage)
//            onResult(null, failureMessage)
//        }
//    })
//}



//fun fetchStockDetails(ticker: String, onResult: (List<StockDetail>?, String?) -> Unit) {
//    val call = PolygonApiClient.instance.getStockDetails(ticker)
//
//    call.enqueue(object : Callback<PolygonResponse> {
//        override fun onResponse(call: Call<PolygonResponse>, response: Response<PolygonResponse>) {
//            if (response.isSuccessful) {
//                onResult(response.body()?.results, null)
//            } else {
//                val errorMessage = "Failed to fetch stock details: Code ${response.code()} - ${response.message()}"
//                Log.e("StockDetails", errorMessage)
//                onResult(null, errorMessage)
//            }
//        }
//
//        override fun onFailure(call: Call<PolygonResponse>, t: Throwable) {
//            val failureMessage = "Failed to fetch stock details: ${t.message}"
//            Log.e("StockDetails", failureMessage)
//            onResult(null, failureMessage)
//        }
//    })
//}
