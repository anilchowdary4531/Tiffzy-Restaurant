package com.tiffzy.restaurant.data.remote

import com.tiffzy.restaurant.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface ApiService {
    @GET("healthz")
    suspend fun checkHealth(): HealthResponse

    @GET("restaurants")
    suspend fun getRestaurants(): List<Restaurant>

    @POST("customer/send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): SendOtpResponse

    @POST("customer/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): VerifyOtpResponse

    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): GenericResponse

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): GenericResponse

    @GET("customer/address")
    suspend fun getAddresses(): AddressListResponse

    @POST("customer/address")
    suspend fun createAddress(@Body request: CreateAddressRequest): Address

    @DELETE("customer/address/{id}")
    suspend fun deleteAddress(@Path("id") id: Int)

    @GET("catalog/search")
    suspend fun searchCatalog(@Query("q") query: String): SearchResponse

    @GET("home")
    suspend fun getHomeData(): HomeResponse

    @GET("restaurants/nearby")
    suspend fun getNearbyRestaurants(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 10
    ): List<Restaurant>

    @GET("restaurants/{slug}")
    suspend fun getRestaurantDetails(@Path("slug") slug: String): RestaurantDetailResponse

    @GET("r/{slug}/menu")
    suspend fun getRestaurantMenu(@Path("slug") slug: String): RestaurantMenuResponse

    @POST("r/{slug}/order")
    suspend fun placeOrder(@Path("slug") slug: String, @Body request: OrderRequest): OrderResponse

    @POST("payments/create")
    suspend fun createPayment(@Body request: CreatePaymentRequest): CreatePaymentResponse

    @POST("payments/verify")
    suspend fun verifyPayment(@Body request: VerifyPaymentRequest): VerifyPaymentResponse

    @GET("customer/profile")
    suspend fun getProfile(): CustomerProfileResponse

    @PUT("customer/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): CustomerProfileResponse

    @GET("customer/orders")
    suspend fun getCustomerOrders(@Query("phone") phone: String): CustomerOrderGroupsResponse

    @POST("customer/fcm-token")
    suspend fun registerFcmToken(@Body request: RegisterFcmTokenRequest)

    // Restaurant Management APIs
    @GET("owner/dashboard/{restaurantId}")
    suspend fun getRestaurantDashboard(@Path("restaurantId") restaurantId: Int): RestaurantDashboardResponse

    @GET("owner/{restaurantId}/analytics")
    suspend fun getRestaurantAnalytics(
        @Path("restaurantId") restaurantId: Int,
        @Query("range") range: String = "24h"
    ): AnalyticsResponse

    @GET("owner/{restaurantId}/settings")
    suspend fun getRestaurantSettings(@Path("restaurantId") restaurantId: Int): RestaurantSettingsResponse

    @PUT("owner/{restaurantId}/settings")
    suspend fun updateRestaurantSettings(
        @Path("restaurantId") restaurantId: Int,
        @Body request: RestaurantSettingsUpdateRequest
    ): RestaurantSettingsResponse

    @GET("orders/live")
    suspend fun getLiveOrders(@Query("status") status: String? = null): LiveOrdersResponse

    @GET("owner/{restaurantId}/orders")
    suspend fun getOwnerOrders(
        @Path("restaurantId") restaurantId: Int,
        @Query("status") status: String? = null,
        @Query("source") source: String? = null,
        @Query("q") query: String? = null
    ): OwnerOrdersResponse

    @PUT("orders/{orderId}/status")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: Int,
        @Body request: UpdateOrderStatusRequest
    ): OrderResponse

    // Menu Management
    @GET("owner/{restaurantId}/menu")
    suspend fun getOwnerMenu(@Path("restaurantId") restaurantId: Int): List<MenuItem>

    @POST("owner/{restaurantId}/menu")
    suspend fun createMenuItem(
        @Path("restaurantId") restaurantId: Int,
        @Body request: MenuRequest
    ): MenuItem

    @PUT("owner/{restaurantId}/menu/{menuId}")
    suspend fun updateMenuItem(
        @Path("restaurantId") restaurantId: Int,
        @Path("menuId") menuId: Int,
        @Body request: MenuRequest
    ): MenuItem

    @DELETE("owner/{restaurantId}/menu/{menuId}")
    suspend fun deleteMenuItem(
        @Path("restaurantId") restaurantId: Int,
        @Path("menuId") menuId: Int
    ): DeleteResponse

    @Multipart
    @POST("owner/{restaurantId}/assets/menu-image")
    suspend fun uploadMenuImage(
        @Path("restaurantId") restaurantId: Int,
        @Part file: MultipartBody.Part
    ): MenuImageUploadResponse
}
