package com.tiffzy.restaurant.data.model

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int,
    val selectedVariant: MenuVariant? = null,
    val selectedAddOns: List<AddOn> = emptyList(),
    val restaurantSlug: String,
    val restaurantName: String
) {
    val totalPrice: Double
        get() {
            val basePrice = selectedVariant?.price ?: menuItem.price
            val addOnsPrice = selectedAddOns.sumOf { it.price }
            return (basePrice + addOnsPrice) * quantity
        }
}

data class Cart(
    val items: List<CartItem> = emptyList(),
    val restaurantSlug: String? = null
) {
    val subtotal: Double
        get() = items.sumOf { it.totalPrice }
    
    val totalCount: Int
        get() = items.sumOf { it.quantity }
}
