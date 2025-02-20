package com.example.jaundicednot

import com.google.gson.annotations.SerializedName

data class ServerResponse(
    val prediction: String,
    @SerializedName("WAY") val way: Double?,
    val severity: String?
)
