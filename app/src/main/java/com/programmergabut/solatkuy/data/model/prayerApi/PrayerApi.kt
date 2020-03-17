package com.programmergabut.solatkuy.data.model.prayerApi


import com.google.gson.annotations.SerializedName

data class PrayerApi(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: List<Data>,
    @SerializedName("status")
    val status: String
)