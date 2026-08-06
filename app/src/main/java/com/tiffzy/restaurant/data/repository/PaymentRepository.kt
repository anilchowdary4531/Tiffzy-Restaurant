package com.tiffzy.restaurant.data.repository

import com.tiffzy.restaurant.core.base.BaseRepository
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {

    suspend fun createPayment(orderId: Int, provider: String): Resource<CreatePaymentResponse> {
        return safeApiCall { 
            apiService.createPayment(CreatePaymentRequest(orderId, "online", provider))
        }
    }

    suspend fun verifyPayment(request: VerifyPaymentRequest): Resource<VerifyPaymentResponse> {
        return safeApiCall { apiService.verifyPayment(request) }
    }
}
