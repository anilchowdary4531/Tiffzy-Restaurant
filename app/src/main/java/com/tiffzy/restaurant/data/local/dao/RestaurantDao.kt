package com.tiffzy.restaurant.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tiffzy.restaurant.data.local.entities.CategoryEntity
import com.tiffzy.restaurant.data.local.entities.RestaurantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    @Query("SELECT * FROM restaurants WHERE type = :type")
    fun getRestaurantsByType(type: String): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE type = 'nearby' ORDER BY id ASC")
    fun getNearbyRestaurantsPaging(): PagingSource<Int, RestaurantEntity>

    @Query("DELETE FROM restaurants WHERE type = :type")
    suspend fun deleteRestaurantsByType(type: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT * FROM categories")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Query("DELETE FROM categories")
    suspend fun clearCategories()
}
