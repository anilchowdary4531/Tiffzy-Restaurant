package com.tiffzy.restaurant.data.model

data class HealthResponse(
    val status: String
)

data class Restaurant(
    val id: Int,
    val name: String,
    val slug: String,
    val city: String?,
    val state: String?,
    val country: String?,
    val pincode: String?,
    val logo: String?,
    val addressLine1: String? = null,
    val bannerUrl: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isActive: Boolean = true,
    val taxEnabled: Boolean = false,
    val taxPercent: Double = 0.0,
    val upiId: String? = null,
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val deliveryTime: String? = null,
    val distance: String? = null,
    val images: List<String> = emptyList(),
    val openingHours: String? = null,
    val categories: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val cuisines: List<String> = emptyList(),
    val averageCost: String? = null
)

data class Review(
    val id: Int,
    val userId: Int? = null,
    val userName: String,
    val userImage: String?,
    val rating: Double,
    val comment: String,
    val images: List<String> = emptyList(),
    val menuItemId: Int? = null,
    val menuItemName: String? = null,
    val date: String
)

data class RestaurantDetailResponse(
    val restaurant: Restaurant,
    val reviews: List<Review>,
    val relatedRestaurants: List<Restaurant>
)
