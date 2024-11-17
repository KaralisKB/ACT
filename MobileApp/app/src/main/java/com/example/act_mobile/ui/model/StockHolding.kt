package com.example.act_mobile.ui.model

data class StockHolding(
    val documentId: String = "",
    val ticker: String,
    val quantity: Double,
    val investedPrice: Double,
    val currentPrice: Double
)
