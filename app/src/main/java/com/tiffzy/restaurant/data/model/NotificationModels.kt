package com.tiffzy.restaurant.data.model

data class RegisterFcmTokenRequest(
    val token: String,
    val platform: String = "android"
)
