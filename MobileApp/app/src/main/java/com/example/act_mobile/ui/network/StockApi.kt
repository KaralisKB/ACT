package com.example.act_mobile.ui.network

import com.example.act_mobile.ui.model.Stock
import retrofit2.http.GET

interface StockApi {
    @GET("/api/stocks")
    suspend fun getStocks(): List<Stock>
}
