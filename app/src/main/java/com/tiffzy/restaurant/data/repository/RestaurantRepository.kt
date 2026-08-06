package com.tiffzy.restaurant.data.repository

import com.tiffzy.restaurant.core.base.BaseRepository
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.dao.MenuItemDao
import com.tiffzy.restaurant.data.local.entities.toDomain
import com.tiffzy.restaurant.data.local.entities.toEntity
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepository @Inject constructor(
    private val apiService: ApiService,
    private val menuItemDao: MenuItemDao
) : BaseRepository() {
    
    fun getCachedMenu(restaurantId: Int): Flow<List<MenuItem>> {
        return menuItemDao.getMenuItems(restaurantId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun refreshMenu(restaurantId: Int): Resource<List<MenuItem>> {
        val result = safeApiCall { apiService.getOwnerMenu(restaurantId) }
        if (result is Resource.Success) {
            menuItemDao.clearMenu(restaurantId)
            menuItemDao.insertAll(result.data.map { it.toEntity() })
        }
        return result
    }

    suspend fun getRestaurantDashboard(restaurantId: Int): Resource<RestaurantDashboardResponse> {
        return safeApiCall { apiService.getRestaurantDashboard(restaurantId) }
    }

    suspend fun getRestaurantAnalytics(restaurantId: Int, range: String = "24h"): Resource<AnalyticsResponse> {
        return safeApiCall { apiService.getRestaurantAnalytics(restaurantId, range) }
    }

    suspend fun getRestaurantSettings(restaurantId: Int): Resource<RestaurantSettingsResponse> {
        return safeApiCall { apiService.getRestaurantSettings(restaurantId) }
    }

    suspend fun updateRestaurantSettings(restaurantId: Int, request: RestaurantSettingsUpdateRequest): Resource<RestaurantSettingsResponse> {
        return safeApiCall { apiService.updateRestaurantSettings(restaurantId, request) }
    }

    suspend fun getLiveOrders(status: String? = null): Resource<LiveOrdersResponse> {
        return safeApiCall { apiService.getLiveOrders(status) }
    }

    suspend fun updateOrderStatus(orderId: Int, status: String, notes: String? = null): Resource<OrderResponse> {
        return safeApiCall { apiService.updateOrderStatus(orderId, UpdateOrderStatusRequest(status, notes)) }
    }

    suspend fun getOwnerOrders(
        restaurantId: Int,
        status: String? = null,
        query: String? = null
    ): Resource<OwnerOrdersResponse> {
        return safeApiCall { apiService.getOwnerOrders(restaurantId, status, null, query) }
    }

    suspend fun createMenuItem(restaurantId: Int, request: MenuRequest): Resource<MenuItem> {
        return safeApiCall { apiService.createMenuItem(restaurantId, request) }
    }

    suspend fun updateMenuItem(restaurantId: Int, menuId: Int, request: MenuRequest): Resource<MenuItem> {
        return safeApiCall { apiService.updateMenuItem(restaurantId, menuId, request) }
    }

    suspend fun deleteMenuItem(restaurantId: Int, menuId: Int): Resource<DeleteResponse> {
        return safeApiCall { apiService.deleteMenuItem(restaurantId, menuId) }
    }

    suspend fun uploadMenuImage(restaurantId: Int, file: MultipartBody.Part): Resource<MenuImageUploadResponse> {
        return safeApiCall { apiService.uploadMenuImage(restaurantId, file) }
    }
}
