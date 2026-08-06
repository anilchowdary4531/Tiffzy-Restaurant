package com.tiffzy.restaurant.data.model

data class ReviewRequest(
    val restaurantId: Int? = null,
    val menuItemId: Int? = null,
    val rating: Double,
    val comment: String,
    val images: List<String> = emptyList()
)

data class ReviewListResponse(
    val reviews: List<Review>,
    val averageRating: Double,
    val totalReviews: Int
)
