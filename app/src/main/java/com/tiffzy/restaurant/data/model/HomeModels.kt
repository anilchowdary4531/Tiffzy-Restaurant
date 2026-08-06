package com.tiffzy.restaurant.data.model

data class Category(
    val id: Int,
    val name: String,
    val image: String
)

data class Offer(
    val id: Int,
    val title: String,
    val description: String,
    val bannerUrl: String,
    val restaurantId: Int? = null,
    val discountCode: String? = null
)

data class HomeResponse(
    val categories: List<Category>,
    val popularRestaurants: List<Restaurant>,
    val recommendedRestaurants: List<Restaurant>,
    val topRatedRestaurants: List<Restaurant>,
    val offers: List<Offer>,
    val recentlyViewed: List<Restaurant>
)
