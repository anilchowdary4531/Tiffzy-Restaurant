package com.tiffzy.restaurant.data.model

data class CreatePaymentRequest(
    val orderId: Int,
    val paymentMethod: String,
    val provider: String
)

data class CreatePaymentResponse(
    val payment: PaymentData,
    val provider: String,
    val razorpay: RazorpayOrderData? = null,
    val phonepe: PhonePePaymentData? = null
)

data class PaymentData(
    val id: Int,
    val orderId: Int,
    val amountSubunit: Long,
    val currency: String,
    val method: String,
    val status: String,
    val provider: String,
    val providerOrderId: String?
)

data class RazorpayOrderData(
    val keyId: String,
    val orderId: String,
    val amount: Long,
    val currency: String
)

data class PhonePePaymentData(
    val base64Payload: String,
    val checksum: String,
    val apiEndPoint: String,
    val merchantId: String,
    val appId: String? = null
)

data class VerifyPaymentRequest(
    val paymentId: Int? = null,
    val orderId: Int? = null,
    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null,
    val razorpaySignature: String? = null,
    val phonepeTransactionId: String? = null,
    val status: String? = null,
    val paymentMode: String? = null
)

data class VerifyPaymentResponse(
    val payment: PaymentData?,
    val verified: Boolean,
    val order: OrderDetails? = null
)

data class SavedCard(
    val id: Int,
    val cardBrand: String,
    val last4: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val holderName: String?
)

data class WalletHistoryResponse(
    val balance: Double,
    val transactions: List<WalletTransaction>
)

data class WalletTransaction(
    val id: Int,
    val amount: Double,
    val type: String, // credit | debit
    val description: String,
    val createdAt: String
)
