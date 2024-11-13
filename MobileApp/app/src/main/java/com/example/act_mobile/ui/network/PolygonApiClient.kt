package com.example.act_mobile.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PolygonApiClient {
    private const val BASE_URL = "https://api.polygon.io/"

    val instance: PolygonApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PolygonApi::class.java)
    }
}
