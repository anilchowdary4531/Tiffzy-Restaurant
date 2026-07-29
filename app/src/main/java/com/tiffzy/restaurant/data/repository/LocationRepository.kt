package com.tiffzy.restaurant.data.repository

import com.tiffzy.restaurant.data.model.Address
import com.tiffzy.restaurant.data.model.CreateAddressRequest
import com.tiffzy.restaurant.data.remote.ApiService

class LocationRepository(private val apiService: ApiService) {

    suspend fun getAddresses(): List<Address> {
        return apiService.getAddresses().addresses
    }

    suspend fun createAddress(request: CreateAddressRequest): Address {
        return apiService.createAddress(request)
    }

    suspend fun deleteAddress(id: Int) {
        apiService.deleteAddress(id)
    }
}
