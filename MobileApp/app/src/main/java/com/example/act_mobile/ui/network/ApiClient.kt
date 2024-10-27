package com.example.act_mobile.network

import com.example.act_mobile.ui.network.StockApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    //   url for stock API
    private val stockRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://act-production-5e24.up.railway.app")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val stockApi: StockApi by lazy {
        stockRetrofit.create(StockApi::class.java)
    }

    // url for news
    private val newsRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val newsApi: NewsApi by lazy {
        newsRetrofit.create(NewsApi::class.java)
    }
}
