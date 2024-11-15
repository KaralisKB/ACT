package com.example.act_mobile.ui.model

data class NewsResponse(
    val articles: List<NewsArticle>
)

data class NewsArticle(
    val title: String,
    val description: String,
    val url: String,
    val urlToImage: String?
)
