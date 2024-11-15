package com.example.act_mobile.network

import com.example.act_mobile.ui.network.NewsApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    // Retrofit instance for News API
    private val newsRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://newsapi.org/") // News API URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // News API instance
    val newsApi: NewsApi by lazy {
        newsRetrofit.create(NewsApi::class.java)
    }
}
