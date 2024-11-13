package com.example.act_mobile.ui.model

import com.google.gson.annotations.SerializedName

data class PreviousCloseResponse(
    val results: List<PreviousCloseResult>?
)

data class PreviousCloseResult(
    @SerializedName("c") val closePrice: Double?
)
