package com.tiffzy.restaurant.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tiffzy.restaurant.data.local.dao.MenuItemDao
import com.tiffzy.restaurant.data.local.entities.MenuItemEntity

@Database(entities = [MenuItemEntity::class], version = 1, exportSchema = false)
abstract class TiffzyDatabase : RoomDatabase() {
    abstract fun menuItemDao(): MenuItemDao
}
