package com.tiffzy.restaurant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tiffzy.restaurant.data.local.dao.MenuItemDao
import com.tiffzy.restaurant.data.local.dao.RemoteKeyDao
import com.tiffzy.restaurant.data.local.dao.RestaurantDao
import com.tiffzy.restaurant.data.local.entities.CategoryEntity
import com.tiffzy.restaurant.data.local.entities.MenuItemEntity
import com.tiffzy.restaurant.data.local.entities.RemoteKey
import com.tiffzy.restaurant.data.local.entities.RestaurantEntity

@Database(
    entities = [
        MenuItemEntity::class,
        RestaurantEntity::class,
        CategoryEntity::class,
        RemoteKey::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TiffzyDatabase : RoomDatabase() {
    abstract fun menuItemDao(): MenuItemDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}
