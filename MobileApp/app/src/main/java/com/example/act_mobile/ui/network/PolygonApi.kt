package com.example.act_mobile.network

import com.example.act_mobile.model.HistoricalDataResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.act_mobile.model.PolygonResponse
import com.example.act_mobile.ui.model.PreviousCloseResponse
import retrofit2.http.Path


//interface PolygonApi {
//    @GET("v3/reference/tickers")
//    fun getStockDetails(
//        @Query("ticker") ticker: String,
//        @Query("apiKey") apiKey: String = "ZT6N7key2jS5_Jz4l6BrlRUIq_9Pknx6" // Replace with your API key
//    ): Call<PolygonResponse>
//}

interface PolygonApi {
    @GET("v3/reference/tickers/{ticker}")
    fun getStockDetails(
        @Path("ticker") ticker: String,
        @Query("apiKey") apiKey: String = "ZT6N7key2jS5_Jz4l6BrlRUIq_9Pknx6"
    ): Call<PolygonResponse>

    @GET("v2/aggs/ticker/{stocksTicker}/prev")
    fun getPreviousClose(
        @Path("stocksTicker") ticker: String,
        @Query("adjusted") adjusted: Boolean = true,
        @Query("apiKey") apiKey: String = "ZT6N7key2jS5_Jz4l6BrlRUIq_9Pknx6"
    ): Call<PreviousCloseResponse>


    @GET("v2/aggs/ticker/{ticker}/range/{multiplier}/{timespan}/{from}/{to}")
    fun getAggregates(
        @Path("ticker") ticker: String,
        @Path("multiplier") multiplier: Int,
        @Path("timespan") timespan: String,
        @Path("from") from: String,
        @Path("to") to: String,
        @Query("apiKey") apiKey: String
    ): Call<HistoricalDataResponse>
}


