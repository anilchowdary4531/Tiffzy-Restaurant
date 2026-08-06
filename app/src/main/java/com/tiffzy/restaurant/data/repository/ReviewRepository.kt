package com.tiffzy.restaurant.data.repository

import com.tiffzy.restaurant.core.base.BaseRepository
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.remote.ApiService
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {

    suspend fun getRestaurantReviews(slug: String, page: Int = 1): Resource<ReviewListResponse> {
        return safeApiCall { apiService.getRestaurantReviews(slug, page) }
    }

    suspend fun addReview(slug: String, request: ReviewRequest): Resource<GenericResponse> {
        return safeApiCall { apiService.addReview(slug, request) }
    }

    suspend fun updateReview(id: Int, request: ReviewRequest): Resource<GenericResponse> {
        return safeApiCall { apiService.updateReview(id, request) }
    }

    suspend fun deleteReview(id: Int): Resource<GenericResponse> {
        return safeApiCall { apiService.deleteReview(id) }
    }

    suspend fun uploadReviewImages(id: Int, images: List<MultipartBody.Part>): Resource<GenericResponse> {
        return safeApiCall { apiService.uploadReviewImages(id, images) }
    }
}
