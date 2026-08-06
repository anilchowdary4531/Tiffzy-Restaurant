package com.tiffzy.restaurant.data.model

data class SendOtpRequest(
    val phone: String,
    val email: String? = null
)

data class SendOtpResponse(
    val message: String,
    val phone: String,
    val expiresAt: String,
    val devOtp: String? = null
)

data class VerifyOtpRequest(
    val phone: String,
    val otp: String,
    val name: String? = null,
    val email: String? = null
)

data class VerifyOtpResponse(
    val message: String,
    val token: String,
    val customer: Customer
)

data class Customer(
    val id: Int,
    val phone: String,
    val name: String?,
    val email: String?,
    val walletBalance: Double = 0.0
)

data class CustomerProfileResponse(
    val customer: Customer
)

data class UpdateProfileRequest(
    val name: String?,
    val email: String?
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val user: StaffUser
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val restaurantName: String
)

data class ForgotPasswordRequest(
    val email: String
)

data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val newPassword: String
)

data class GenericResponse(
    val message: String,
    val success: Boolean = true
)

data class StaffUser(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val role: String,
    val restaurantId: Int?,
    val restaurant: Restaurant? = null
)
