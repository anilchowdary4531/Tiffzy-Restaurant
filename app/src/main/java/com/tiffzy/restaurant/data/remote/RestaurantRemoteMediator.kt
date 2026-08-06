package com.tiffzy.restaurant.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.tiffzy.restaurant.data.local.TiffzyDatabase
import com.tiffzy.restaurant.data.local.entities.RemoteKey
import com.tiffzy.restaurant.data.local.entities.RestaurantEntity
import com.tiffzy.restaurant.data.local.entities.toEntity

@OptIn(ExperimentalPagingApi::class)
class RestaurantRemoteMediator(
    private val apiService: ApiService,
    private val database: TiffzyDatabase,
    private val lat: Double,
    private val lng: Double
) : RemoteMediator<Int, RestaurantEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RestaurantEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyAtClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = apiService.getNearbyRestaurants(lat, lng, page, state.config.pageSize)
            val endOfPaginationReached = response.isEmpty()
            
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeyDao().clearRemoteKeys()
                    database.restaurantDao().deleteRestaurantsByType("nearby")
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = response.map {
                    RemoteKey(id = it.id.toString(), prevKey = prevKey, nextKey = nextKey)
                }
                database.remoteKeyDao().insertAll(keys)
                database.restaurantDao().insertRestaurants(response.map { it.toEntity("nearby") })
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, RestaurantEntity>): RemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { restaurant ->
                database.remoteKeyDao().getRemoteKeysId(restaurant.id.toString())
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, RestaurantEntity>): RemoteKey? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { restaurant ->
                database.remoteKeyDao().getRemoteKeysId(restaurant.id.toString())
            }
    }

    private suspend fun getRemoteKeyAtClosestToCurrentPosition(state: PagingState<Int, RestaurantEntity>): RemoteKey? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                database.remoteKeyDao().getRemoteKeysId(id.toString())
            }
        }
    }
}
