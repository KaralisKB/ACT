package com.example.act_mobile.model

import com.google.gson.annotations.SerializedName

data class HistoricalDataResponse(
    val status: String?,
    val results: List<AggregateResult>?
)


data class AggregateResult(
    @SerializedName("c") val closePrice: Double?,
    @SerializedName("h") val highPrice: Double?,
    @SerializedName("l") val lowPrice: Double?,
    @SerializedName("o") val openPrice: Double?,
    @SerializedName("t") val timestamp: Long?,
    @SerializedName("v") val volume: Long?
)
