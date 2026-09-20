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

    @PUT("customer/address/{id}")
    suspend fun updateAddress(@Path("id") id: Int, @Body request: CreateAddressRequest): Address

    @PATCH("customer/address/{id}/default")
    suspend fun setDefaultAddress(@Path("id") id: Int): GenericResponse

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
        @Query("limit") limit: Int = 10,
        @Query("category") category: String? = null
    ): List<Restaurant>

    @GET("restaurants/{slug}")
    suspend fun getRestaurantDetails(@Path("slug") slug: String): RestaurantDetailResponse

    @GET("r/{slug}/menu")
    suspend fun getRestaurantMenu(@Path("slug") slug: String): RestaurantMenuResponse

    @POST("customer/apply-coupon")
    suspend fun applyCoupon(@Body request: CouponRequest): Coupon

    @POST("r/{slug}/order")
    suspend fun placeOrder(@Path("slug") slug: String, @Body request: OrderRequest): OrderResponse

    @GET("customer/orders/{id}")
    suspend fun getOrderDetails(@Path("id") id: Int): OrderResponse

    @POST("customer/orders/{id}/cancel")
    suspend fun cancelOrder(@Path("id") id: Int): GenericResponse

    @POST("customer/orders/{id}/reorder")
    suspend fun reorder(@Path("id") id: Int): OrderResponse

    @GET("customer/orders/{id}/invoice")
    suspend fun getInvoiceUrl(@Path("id") id: Int): GenericResponse

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

    @GET("customer/notifications")
    suspend fun getNotifications(): NotificationListResponse

    @PATCH("customer/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: Int): GenericResponse

    @DELETE("customer/notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: Int): GenericResponse

    @DELETE("customer/account")
    suspend fun deleteAccount(): GenericResponse

    @Multipart
    @POST("customer/profile/picture")
    suspend fun uploadProfilePicture(@Part file: MultipartBody.Part): GenericResponse

    @GET("customer/wallet/history")
    suspend fun getWalletHistory(): WalletHistoryResponse

    @POST("customer/wallet/recharge")
    suspend fun rechargeWallet(@Body request: RechargeRequest): RechargeResponse

    @POST("customer/wallet/verify")
    suspend fun verifyRecharge(@Body request: VerifyPaymentRequest): VerifyPaymentResponse

    @GET("customer/cards")
    suspend fun getSavedCards(): List<SavedCard>

    // Review APIs
    @GET("restaurants/{slug}/reviews")
    suspend fun getRestaurantReviews(
        @Path("slug") slug: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): ReviewListResponse

    @POST("restaurants/{slug}/reviews")
    suspend fun addReview(
        @Path("slug") slug: String,
        @Body request: ReviewRequest
    ): GenericResponse

    @PUT("customer/reviews/{id}")
    suspend fun updateReview(
        @Path("id") id: Int,
        @Body request: ReviewRequest
    ): GenericResponse

    @DELETE("customer/reviews/{id}")
    suspend fun deleteReview(@Path("id") id: Int): GenericResponse

    @Multipart
    @POST("customer/reviews/{id}/images")
    suspend fun uploadReviewImages(
        @Path("id") id: Int,
        @Part images: List<MultipartBody.Part>
    ): GenericResponse

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

    // Table Management
    @GET("owner/{restaurantId}/tables")
    suspend fun getTables(
        @Path("restaurantId") restaurantId: Int
    ): List<TableSection>

    @POST("owner/{restaurantId}/sections")
    suspend fun createSection(
        @Path("restaurantId") restaurantId: Int,
        @Body request: CreateSectionRequest
    ): TableSection

    @POST("owner/{restaurantId}/tables")
    suspend fun createTable(
        @Path("restaurantId") restaurantId: Int,
        @Body request: CreateTableRequest
    ): TableData

    @PUT("owner/{restaurantId}/tables/{tableId}")
    suspend fun updateTable(
        @Path("restaurantId") restaurantId: Int,
        @Path("tableId") tableId: Int,
        @Body request: CreateTableRequest
    ): TableData

    @DELETE("owner/{restaurantId}/tables/{tableId}")
    suspend fun deleteTable(
        @Path("restaurantId") restaurantId: Int,
        @Path("tableId") tableId: Int
    ): GenericResponse

    // Staff Management
    @GET("owner/{restaurantId}/staff")
    suspend fun getStaff(
        @Path("restaurantId") restaurantId: Int
    ): StaffListResponse

    @POST("owner/{restaurantId}/staff")
    suspend fun createStaff(
        @Path("restaurantId") restaurantId: Int,
        @Body request: CreateStaffRequest
    ): StaffMember

    @PATCH("owner/{restaurantId}/staff/{staffId}/status")
    suspend fun toggleStaffStatus(
        @Path("restaurantId") restaurantId: Int,
        @Path("staffId") staffId: Int,
        @Body request: Map<String, Boolean>
    ): StaffMember

    @PUT("owner/{restaurantId}/staff/{staffId}")
    suspend fun updateStaff(
        @Path("restaurantId") restaurantId: Int,
        @Path("staffId") staffId: Int,
        @Body request: CreateStaffRequest
    ): StaffMember

    @GET("owner/{restaurantId}/staff/{staffId}/access-link")
    suspend fun getStaffAccessLink(
        @Path("restaurantId") restaurantId: Int,
        @Path("staffId") staffId: Int
    ): StaffAccessLinkResponse

    // Pay Later (Khata)
    @GET("owner/{restaurantId}/pay-later")
    suspend fun getPayLaterAccounts(
        @Path("restaurantId") restaurantId: Int
    ): PayLaterListResponse

    @POST("owner/{restaurantId}/pay-later")
    suspend fun createPayLaterAccount(
        @Path("restaurantId") restaurantId: Int,
        @Body request: CreatePayLaterRequest
    ): PayLaterAccount

    // Supply Marketplace
    @GET("supply/products")
    suspend fun getSupplyProducts(
        @Query("q") query: String? = null,
        @Query("category") category: String? = null
    ): SupplyProductResponse

    @GET("owner/{restaurantId}/supply-orders")
    suspend fun getSupplyOrders(
        @Path("restaurantId") restaurantId: Int
    ): SupplyOrderListResponse
}
