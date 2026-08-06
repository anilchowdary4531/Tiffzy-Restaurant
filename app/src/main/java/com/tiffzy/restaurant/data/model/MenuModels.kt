package com.tiffzy.restaurant.data.model

data class RestaurantMenuResponse(
    val restaurant: Restaurant,
    val menu: List<MenuItem>
)

data class MenuItem(
    val id: Int,
    val name: String,
    val description: String?,
    val category: String,
    val image: String?,
    val price: Double,
    val isAvailable: Boolean,
    val isFeatured: Boolean,
    val rating: Double,
    val reviewCount: Int,
    val orderCount: Int,
    val isVeg: Boolean = true,
    val isBestSeller: Boolean = false,
    val variants: List<MenuVariant> = emptyList(),
    val addOns: List<AddOn> = emptyList(),
    val isFavorite: Boolean = false
)

data class MenuVariant(
    val id: Int,
    val name: String,
    val price: Double,
    val isAvailable: Boolean = true
)

data class AddOn(
    val id: Int,
    val name: String,
    val price: Double,
    val isAvailable: Boolean = true
)
