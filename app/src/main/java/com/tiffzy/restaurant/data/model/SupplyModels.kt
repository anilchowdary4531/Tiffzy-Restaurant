package com.tiffzy.restaurant.data.model

data class SupplyProduct(
    val id: Int = 0,
    val name: String = "",
    val description: String? = null,
    val image: String? = null,
    val price: Double = 0.0,
    val unit: String = "",
    val category: String = "",
    val supplierName: String = ""
)

data class SupplyProductResponse(
    val products: List<SupplyProduct> = emptyList()
)

data class SupplyOrder(
    val id: Int = 0,
    val orderNo: String = "",
    val status: String = "",
    val totalAmount: Double = 0.0,
    val itemsCount: Int = 0,
    val createdAt: String = ""
)

data class SupplyOrderListResponse(
    val orders: List<SupplyOrder> = emptyList()
)
