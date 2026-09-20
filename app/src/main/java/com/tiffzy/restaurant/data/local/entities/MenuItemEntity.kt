package com.tiffzy.restaurant.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tiffzy.restaurant.data.model.MenuItem

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey val id: Int,
    val restaurantId: Int,
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
    val isVeg: Boolean,
    val isBestSeller: Boolean,
    val isFavorite: Boolean
)

fun MenuItemEntity.toDomain(): MenuItem {
    return MenuItem(
        id = id,
        restaurantId = restaurantId,
        name = name,
        description = description,
        category = category,
        image = image,
        price = price,
        isAvailable = isAvailable,
        isFeatured = isFeatured,
        rating = rating,
        reviewCount = reviewCount,
        orderCount = orderCount,
        isVeg = isVeg,
        isBestSeller = isBestSeller,
        isFavorite = isFavorite
    )
}

fun MenuItem.toEntity(): MenuItemEntity {
    return MenuItemEntity(
        id = id,
        restaurantId = restaurantId,
        name = name,
        description = description,
        category = category,
        image = image,
        price = price,
        isAvailable = isAvailable,
        isFeatured = isFeatured,
        rating = rating,
        reviewCount = reviewCount,
        orderCount = orderCount,
        isVeg = isVeg,
        isBestSeller = isBestSeller,
        isFavorite = isFavorite
    )
}
