package com.tiffzy.restaurant.data.model

data class TableSection(
    val id: Int = 0,
    val name: String = "",
    val tables: List<TableData> = emptyList()
)

data class TableData(
    val id: Int = 0,
    val number: String = "",
    val sectionId: Int = 0,
    val sectionName: String? = null,
    val seats: Int = 0,
    val status: String = "BLANK", // BLANK, RUNNING, PRINTED, PAID, RUNNING_KOT
    val currentOrderId: Int? = null,
    val currentOrderTotal: Double? = null,
    val qrCodeUrl: String? = null,
    val isActive: Boolean = true
)

data class TableGroupResponse(
    val sections: List<TableSection> = emptyList()
)

data class CreateTableRequest(
    val number: String,
    val sectionId: Int,
    val seats: Int,
    val isActive: Boolean = true
)

data class CreateSectionRequest(
    val name: String
)
