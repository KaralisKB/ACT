package com.example.act_mobile.network

import retrofit2.http.GET
import retrofit2.http.Query

data class NewsArticle(
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String?
)

data class NewsResponse(
    val articles: List<NewsArticle>
)

interface NewsApi {
    @GET("v2/everything")
    suspend fun getMarketNews(
        @Query("q") query: String = "stocks market investments crypto",
        @Query("apiKey") apiKey: String
    ): NewsResponse
}
