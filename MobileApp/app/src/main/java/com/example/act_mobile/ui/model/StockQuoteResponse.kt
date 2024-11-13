package com.example.act_mobile.ui.model

data class StockQuoteResponse(
    val quoteResponse: QuoteResponse
)

data class QuoteResponse(
    val result: List<StockQuote>
)

data class StockQuote(
    val symbol: String,
    val shortName: String?,
    val regularMarketPrice: Double?,
    val regularMarketChange: Double?,
    val regularMarketChangePercent: Double?,
    val regularMarketVolume: Long?,
    val regularMarketDayHigh: Double?,
    val regularMarketDayLow: Double?
)
