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
    val isAvailable: Boolean
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
        isAvailable = isAvailable
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
        isAvailable = isAvailable
    )
}
