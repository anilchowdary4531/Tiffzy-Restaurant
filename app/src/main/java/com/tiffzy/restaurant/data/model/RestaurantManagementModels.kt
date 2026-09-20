package com.tiffzy.restaurant.data.model

data class RestaurantDashboardResponse(
    val restaurantId: Int,
    val restaurantName: String,
    val menuCount: Int,
    val ordersCount: Int,
    val tablesCount: Int,
    val revenue: Double,
    val taxEnabled: Boolean,
    val taxPercent: Double,
    val recentOrders: List<OrderDetails>
)

data class LiveOrdersResponse(
    val orders: List<OrderDetails>
)

data class OwnerOrdersResponse(
    val orders: List<OrderDetails>
)

data class UpdateOrderStatusRequest(
    val status: String,
    val notes: String? = null,
    val changedByName: String? = null
)

data class AnalyticsResponse(
    val generatedAt: String,
    val range: String,
    val overview: AnalyticsOverview,
    val realtime: AnalyticsRealtime,
    val statusFunnel: List<StatusCount>,
    val revenueWaveform: List<DataPoint> = emptyList(),
    val aiDemandRadar: AIDemandRadar? = null,
    val peakHours: List<HourlyLoad> = emptyList(),
    val categoryMix: List<CategoryRevenue> = emptyList(),
    val payments: List<PaymentBreakdown> = emptyList(),
    val fulfillment: List<ChannelBreakdown> = emptyList(),
    val topItems: List<TopMenuItem> = emptyList(),
    val tableHeatmap: List<TableTurnData> = emptyList()
)

data class DataPoint(
    val label: String,
    val value: Double,
    val secondaryValue: Double? = null
)

data class AIDemandRadar(
    val runRate: Double,
    val revenuePerHour: Double,
    val projectionEndOfDay: Double,
    val confidence: String, // HIGH, MEDIUM, LOW
    val peakRushWindow: String
)

data class HourlyLoad(
    val hour: Int,
    val orderCount: Int,
    val isPeak: Boolean = false
)

data class CategoryRevenue(
    val name: String,
    val revenue: Double,
    val percentage: Float
)

data class PaymentBreakdown(
    val method: String, // UPI, Card, Cash, Wallet
    val count: Int,
    val amount: Double,
    val percentage: Float
)

data class ChannelBreakdown(
    val channel: String, // Dine-in, Takeaway, Delivery
    val count: Int,
    val amount: Double,
    val percentage: Float
)

data class TopMenuItem(
    val rank: Int,
    val name: String,
    val units: Int,
    val revenue: Double,
    val avgPrice: Double
)

data class TableTurnData(
    val tableNo: String,
    val turns: Int,
    val revenue: Double
)

data class AnalyticsOverview(
    val totalOrders: Int,
    val totalRevenue: Double,
    val avgOrderValue: Double,
    val deliveredOrders: Int,
    val cancelledOrders: Int,
    val revenueChange: String = "0%",
    val ordersChange: String = "0%",
    val repeatRate: String = "0%"
)

data class AnalyticsRealtime(
    val activeQueue: Int,
    val delayedTickets: Int,
    val activeTables: Int,
    val totalTables: Int,
    val totalMenuItems: Int
)

data class StatusCount(
    val status: String,
    val count: Int
)

data class RestaurantSettingsResponse(
    val restaurant: RestaurantSettings
)

data class RestaurantSettings(
    val id: Int,
    val name: String,
    val legalName: String? = null,
    val ownerName: String? = null,
    val slug: String,
    val isActive: Boolean,
    val phone: String?,
    val email: String?,
    val upiId: String? = null,
    val addressLine1: String? = null,
    val city: String?,
    val state: String? = null,
    val country: String? = null,
    val pincode: String? = null,
    val gstNumber: String? = null,
    val logo: String? = null,
    val bannerUrl: String? = null,
    val currency: String? = null
)

data class MenuRequest(
    val name: String,
    val description: String?,
    val category: String,
    val image: String?,
    val price: Double,
    val isAvailable: Boolean
)

data class MenuImageUploadResponse(
    val message: String,
    val upload: MenuImageUploadData
)

data class MenuImageUploadData(
    val publicUrl: String,
    val key: String
)

data class DeleteResponse(
    val message: String
)

data class RestaurantSettingsUpdateRequest(
    val name: String? = null,
    val legalName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val upiId: String? = null,
    val addressLine1: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val isActive: Boolean? = null
)
