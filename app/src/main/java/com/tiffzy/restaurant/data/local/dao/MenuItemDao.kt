package com.tiffzy.restaurant.data.local.dao

import androidx.room.*
import com.tiffzy.restaurant.data.local.entities.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId")
    fun getMenuItems(restaurantId: Int): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MenuItemEntity>)

    @Query("DELETE FROM menu_items WHERE restaurantId = :restaurantId")
    suspend fun clearMenu(restaurantId: Int)
}
