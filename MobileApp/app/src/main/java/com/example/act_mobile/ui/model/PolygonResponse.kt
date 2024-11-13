package com.example.act_mobile.model

import com.google.gson.annotations.SerializedName

data class PolygonResponse(
    val request_id: String?,
    val results: StockDetail?,
    val status: String?

)

data class StockDetail(
    val active: Boolean?,
    val address: Address?,
    val branding: Branding?,
    val cik: String?,
    @SerializedName("composite_figi") val compositeFigi: String?,
    @SerializedName("currency_name") val currency: String?,
    val description: String?,
    @SerializedName("market_cap") val marketCap: Double?,
    val name: String?,
    @SerializedName("primary_exchange") val primaryExchange: String?,
    val ticker: String?,
    val iconUrl: String? = branding?.iconUrl,
    val logoUrl: String? = branding?.logoUrl,
    val type: String?,
    var currentPrice: Double? = null,
    @SerializedName("o") val openPrice: Double?,
    @SerializedName("h") val highPrice: Double?,
    @SerializedName("l") val lowPrice: Double?,
    @SerializedName("v") val volume: Long?
)

data class Address(
    val address1: String?,
    val city: String?,
    val postal_code: String?,
    val state: String?

)

data class Branding(
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("logo_url") val logoUrl: String?
)




//data class PolygonResponse(
//    val count: Int?,
//    val results: List<StockDetail>?,
//    val status: String?
//)
//
//data class StockDetail(
//    val active: Boolean?,
//    val cik: String?,
//    @SerializedName("composite_figi") val compositeFigi: String?,
//    @SerializedName("currency_name") val currency: String?,
//    @SerializedName("last_updated_utc") val lastUpdated: String?,
//    val locale: String?,
//    val market: String?,
//    val name: String?,
//    @SerializedName("primary_exchange") val primaryExchange: String?,
//    @SerializedName("share_class_figi") val shareClassFigi: String?,
//    val ticker: String?,
//    val type: String?
//)
