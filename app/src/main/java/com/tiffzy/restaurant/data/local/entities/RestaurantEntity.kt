package com.tiffzy.restaurant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiffzy.restaurant.data.model.Restaurant

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val slug: String,
    val city: String?,
    val logo: String?,
    val bannerUrl: String?,
    val rating: Double = 0.0,
    val deliveryTime: String? = null,
    val distance: String? = null,
    val type: String // popular, recommended, nearby, etc.
)

fun Restaurant.toEntity(type: String): RestaurantEntity {
    return RestaurantEntity(
        id = id,
        name = name,
        slug = slug,
        city = city,
        logo = logo,
        bannerUrl = bannerUrl,
        type = type
    )
}

fun RestaurantEntity.toDomain(): Restaurant {
    return Restaurant(
        id = id,
        name = name,
        slug = slug,
        city = city,
        state = null,
        country = null,
        pincode = null,
        logo = logo,
        bannerUrl = bannerUrl
    )
}
