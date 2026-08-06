package com.tiffzy.restaurant.data.model

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int,
    val selectedVariant: MenuVariant? = null,
    val selectedAddOns: List<AddOn> = emptyList(),
    val restaurantSlug: String,
    val restaurantName: String
) {
    val unitPrice: Double
        get() {
            val basePrice = selectedVariant?.price ?: menuItem.price
            val addOnsPrice = selectedAddOns.sumOf { it.price }
            return basePrice + addOnsPrice
        }

    val totalPrice: Double
        get() = unitPrice * quantity
}

data class Coupon(
    val code: String,
    val discountAmount: Double,
    val minOrderAmount: Double,
    val description: String
)

data class CouponRequest(
    val code: String,
    val cartSubtotal: Double,
    val restaurantSlug: String
)

data class Cart(
    val items: List<CartItem> = emptyList(),
    val restaurantSlug: String? = null,
    val restaurantName: String? = null,
    val appliedCoupon: Coupon? = null,
    val deliveryCharge: Double = 30.0,
    val packingCharge: Double = 15.0,
    val gstRate: Double = 0.05 // 5% GST
) {
    val subtotal: Double
        get() = items.sumOf { it.totalPrice }
    
    val totalCount: Int
        get() = items.sumOf { it.quantity }

    val discount: Double
        get() = appliedCoupon?.discountAmount ?: 0.0

    val gstAmount: Double
        get() = (subtotal - discount) * gstRate

    val grandTotal: Double
        get() = subtotal - discount + gstAmount + deliveryCharge + packingCharge
}
