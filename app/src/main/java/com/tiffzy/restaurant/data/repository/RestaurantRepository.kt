package com.tiffzy.restaurant.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.tiffzy.restaurant.core.base.BaseRepository
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.TiffzyDatabase
import com.tiffzy.restaurant.data.local.dao.MenuItemDao
import com.tiffzy.restaurant.data.local.dao.RestaurantDao
import com.tiffzy.restaurant.data.local.entities.toDomain
import com.tiffzy.restaurant.data.local.entities.toEntity
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.remote.ApiService
import com.tiffzy.restaurant.data.remote.RestaurantRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepository @Inject constructor(
    private val apiService: ApiService,
    private val menuItemDao: MenuItemDao,
    private val restaurantDao: RestaurantDao,
    private val database: TiffzyDatabase
) : BaseRepository() {
    
    // Home Data
    suspend fun getHomeData(): Resource<HomeResponse> {
        val result = safeApiCall { apiService.getHomeData() }
        if (result is Resource.Success) {
            // Cache data
            restaurantDao.clearCategories()
            restaurantDao.insertCategories(result.data.categories.map { it.toEntity() })
            
            restaurantDao.deleteRestaurantsByType("popular")
            restaurantDao.insertRestaurants(result.data.popularRestaurants.map { it.toEntity("popular") })
            
            restaurantDao.deleteRestaurantsByType("recommended")
            restaurantDao.insertRestaurants(result.data.recommendedRestaurants.map { it.toEntity("recommended") })
        }
        return result
    }

    fun getNearbyRestaurants(lat: Double, lng: Double): Flow<PagingData<Restaurant>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = 10, prefetchDistance = 2),
            remoteMediator = RestaurantRemoteMediator(apiService, database, lat, lng),
            pagingSourceFactory = { restaurantDao.getNearbyRestaurantsPaging() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    suspend fun searchCatalog(query: String): Resource<SearchResponse> {
        return safeApiCall { apiService.searchCatalog(query) }
    }

    suspend fun getRestaurantDetails(slug: String): Resource<RestaurantDetailResponse> {
        return safeApiCall { apiService.getRestaurantDetails(slug) }
    }

    suspend fun getRestaurantMenu(slug: String): Resource<RestaurantMenuResponse> {
        return safeApiCall { apiService.getRestaurantMenu(slug) }
    }

    suspend fun placeOrder(slug: String, request: OrderRequest): Resource<OrderResponse> {
        return safeApiCall { apiService.placeOrder(slug, request) }
    }

    suspend fun getCustomerOrders(phone: String): Resource<CustomerOrderGroupsResponse> {
        return safeApiCall { apiService.getCustomerOrders(phone) }
    }

    suspend fun getOrderDetails(orderId: Int): Resource<OrderResponse> {
        return safeApiCall { apiService.getOrderDetails(orderId) }
    }

    suspend fun cancelOrder(orderId: Int): Resource<GenericResponse> {
        return safeApiCall { apiService.cancelOrder(orderId) }
    }

    suspend fun reorder(orderId: Int): Resource<OrderResponse> {
        return safeApiCall { apiService.reorder(orderId) }
    }

    suspend fun getInvoiceUrl(orderId: Int): Resource<GenericResponse> {
        return safeApiCall { apiService.getInvoiceUrl(orderId) }
    }

    // Existing Owner logic...
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
