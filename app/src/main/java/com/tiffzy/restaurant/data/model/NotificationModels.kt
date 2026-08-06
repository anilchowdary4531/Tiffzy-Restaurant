package com.tiffzy.restaurant.data.model

data class RegisterFcmTokenRequest(
    val token: String,
    val platform: String = "android"
)

data class Notification(
    val id: Int,
    val title: String,
    val body: String,
    val type: String, // order_update | offer | promotion
    val metadata: Map<String, String>? = null,
    val isRead: Boolean,
    val createdAt: String
)

data class NotificationListResponse(
    val notifications: List<Notification>
)
