package com.example.act_mobile.ui.model

data class Stock(
    val id: String,
    val name: String,
    val marketCap: Long,
    val sector: String,
    val symbol: String,
    val price: Double,
    val lastUpdated: Timestamp
)

data class Timestamp(
    val _seconds: Long,
    val _nanoseconds: Int
)
