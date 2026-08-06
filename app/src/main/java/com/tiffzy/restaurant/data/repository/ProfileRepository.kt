package com.tiffzy.restaurant.data.repository

import com.tiffzy.restaurant.core.base.BaseRepository
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.remote.ApiService
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {

    suspend fun getProfile(): Resource<CustomerProfileResponse> {
        return safeApiCall { apiService.getProfile() }
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Resource<CustomerProfileResponse> {
        return safeApiCall { apiService.updateProfile(request) }
    }

    suspend fun uploadProfilePicture(file: MultipartBody.Part): Resource<GenericResponse> {
        return safeApiCall { apiService.uploadProfilePicture(file) }
    }

    suspend fun getWalletHistory(): Resource<WalletHistoryResponse> {
        return safeApiCall { apiService.getWalletHistory() }
    }

    suspend fun getSavedCards(): Resource<List<SavedCard>> {
        return safeApiCall { apiService.getSavedCards() }
    }

    suspend fun deleteAccount(): Resource<GenericResponse> {
        return safeApiCall { apiService.deleteAccount() }
    }
}
