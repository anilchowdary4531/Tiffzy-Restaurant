package com.tiffzy.restaurant.data.model

data class StaffMember(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val isActive: Boolean = true,
    val permissions: List<String> = emptyList(),
    val createdAt: String = ""
)

data class StaffListResponse(
    val staff: List<StaffMember> = emptyList()
)

data class CreateStaffRequest(
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val permissions: List<String>
)

data class StaffAccessLinkResponse(
    val accessLink: String = ""
)
