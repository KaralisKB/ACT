package com.example.act_mobile.ui.network

import com.example.act_mobile.ui.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("v2/everything")
    suspend fun getMarketNews(
        @Query("q") query: String = "stocks market investments crypto",
        @Query("apiKey") apiKey: String
    ): NewsResponse
}
