package com.tiffzy.restaurant.data.repository

import com.tiffzy.restaurant.core.base.BaseRepository
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.GenericResponse
import com.tiffzy.restaurant.data.model.Notification
import com.tiffzy.restaurant.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {

    suspend fun getNotifications(): Resource<List<Notification>> {
        return safeApiCall { apiService.getNotifications().notifications }
    }

    suspend fun markAsRead(id: Int): Resource<GenericResponse> {
        return safeApiCall { apiService.markAsRead(id) }
    }

    suspend fun deleteNotification(id: Int): Resource<GenericResponse> {
        return safeApiCall { apiService.deleteNotification(id) }
    }
}
