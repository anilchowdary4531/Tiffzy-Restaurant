package com.tiffzy.restaurant.data.model

data class CartItem(
    val menuItem: MenuItem,
    var quantity: Int,
    val restaurantSlug: String,
    val restaurantName: String
)
