package com.example.jaundicednot

import com.google.gson.annotations.SerializedName

data class ServerResponse(
    val prediction: String,
    val severity: String
)
