package com.tiffzy.restaurant.data.repository

import com.tiffzy.restaurant.core.base.BaseRepository
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.remote.ApiService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : BaseRepository() {

    suspend fun sendOtp(phone: String, email: String? = null): Resource<SendOtpResponse> {
        return safeApiCall { apiService.sendOtp(SendOtpRequest(phone, email)) }
    }

    suspend fun verifyOtp(phone: String, otp: String, name: String? = null, email: String? = null): Resource<VerifyOtpResponse> {
        val result = safeApiCall { apiService.verifyOtp(VerifyOtpRequest(phone, otp, name, email)) }
        if (result is Resource.Success) {
            sessionManager.saveAuthToken(result.data.token)
            sessionManager.saveUserInfo(result.data.customer.name, result.data.customer.phone, "customer")
        }
        return result
    }

    suspend fun login(email: String, password: String): Resource<LoginResponse> {
        val result = safeApiCall { apiService.login(LoginRequest(email, password)) }
        if (result is Resource.Success) {
            sessionManager.saveAuthToken(result.data.token)
            sessionManager.saveStaffInfo(result.data.user.name, result.data.user.role, result.data.user.restaurantId)
        }
        return result
    }

    suspend fun registerFcmToken(token: String): Resource<Unit> {
        return safeApiCall { apiService.registerFcmToken(RegisterFcmTokenRequest(token)) }
    }

    suspend fun getAuthToken(): String? = sessionManager.authToken.first()
    
    suspend fun getAccountType(): String? = sessionManager.accountType.first()

    suspend fun logout() {
        sessionManager.logout()
    }
}
