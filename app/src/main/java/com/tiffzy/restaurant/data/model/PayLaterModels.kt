package com.tiffzy.restaurant.data.model

data class PayLaterAccount(
    val id: Int,
    val customerName: String,
    val customerPhone: String,
    val outstandingBalance: Double,
    val limit: Double,
    val status: String, // PENDING, APPROVED, BLOCKED
    val lastTransactionAt: String?
)

data class PayLaterListResponse(
    val accounts: List<PayLaterAccount>
)

data class CreatePayLaterRequest(
    val customerPhone: String,
    val limit: Double
)
