package com.tiffzy.restaurant.data.repository

import com.tiffzy.restaurant.core.base.BaseRepository
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.remote.ApiService
import com.tiffzy.restaurant.util.LocationHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val apiService: ApiService,
    private val locationHelper: LocationHelper
) : BaseRepository() {

    suspend fun getSavedAddresses(): Resource<List<Address>> {
        return safeApiCall { apiService.getAddresses().addresses }
    }

    suspend fun createAddress(request: CreateAddressRequest): Resource<Address> {
        return safeApiCall { apiService.createAddress(request) }
    }

    suspend fun updateAddress(id: Int, request: CreateAddressRequest): Resource<Address> {
        return safeApiCall { apiService.updateAddress(id, request) }
    }

    suspend fun deleteAddress(id: Int): Resource<Unit> {
        return safeApiCall { apiService.deleteAddress(id) }
    }

    suspend fun setDefaultAddress(id: Int): Resource<GenericResponse> {
        return safeApiCall { apiService.setDefaultAddress(id) }
    }

    suspend fun getCurrentLocation() = locationHelper.getCurrentLocation()

    suspend fun getAddressFromLocation(lat: Double, lng: Double) = 
        locationHelper.getAddressFromLocation(lat, lng)

    fun isLocationPermissionGranted() = locationHelper.isLocationPermissionGranted()
}
