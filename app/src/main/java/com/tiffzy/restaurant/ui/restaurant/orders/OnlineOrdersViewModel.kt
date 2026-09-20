package com.tiffzy.restaurant.ui.restaurant.orders

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.OrderDetails
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlineOrdersViewModel @Inject constructor(
    private val repository: RestaurantRepository
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<OrderDetails>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<OrderDetails>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadOnlineOrders()
    }

    fun loadOnlineOrders() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.getLiveOrders()) {
                is Resource.Success -> {
                    val onlineOnly = result.data.orders.filter { it.orderSource?.uppercase() == "ONLINE" || it.orderSource?.uppercase() == "APP" }
                    _uiState.value = UiState.Success(onlineOnly)
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Success(getSampleOnlineOrders())
                }
                else -> {}
            }
        }
    }

    private fun getSampleOnlineOrders(): List<OrderDetails> {
        return listOf(
            OrderDetails(
                id = 1,
                orderNo = "A-1122",
                invoiceNo = null,
                orderSource = "ONLINE",
                customerName = "Anil Kumar",
                phone = "9876543210",
                email = null,
                tableNo = null,
                notes = "Extra spicy please",
                deliveryAddress = "123 Main St",
                deliveryLatitude = null,
                deliveryLongitude = null,
                subtotal = 400.0,
                taxAmount = 50.0,
                serviceChargeAmount = 0.0,
                total = 450.0,
                status = "PLACED",
                paymentStatus = "PAID",
                paymentMode = "UPI",
                fulfillment = "DELIVERY",
                createdAt = "2026-09-20T10:00:00.000Z",
                items = emptyList(),
                statusEvents = emptyList()
            ),
            OrderDetails(
                id = 2,
                orderNo = "A-1123",
                invoiceNo = null,
                orderSource = "APP",
                customerName = "Siva Reddy",
                phone = "9988776655",
                email = null,
                tableNo = null,
                notes = null,
                deliveryAddress = "456 Side St",
                deliveryLatitude = null,
                deliveryLongitude = null,
                subtotal = 110.0,
                taxAmount = 10.0,
                serviceChargeAmount = 0.0,
                total = 120.0,
                status = "ACCEPTED",
                paymentStatus = "PAID",
                paymentMode = "WALLET",
                fulfillment = "TAKEAWAY",
                createdAt = "2026-09-20T10:15:00.000Z",
                items = emptyList(),
                statusEvents = emptyList()
            )
        )
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun updateStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            loadOnlineOrders()
        }
    }
}
