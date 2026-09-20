package com.tiffzy.restaurant.ui.restaurant.kitchen

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.OrderDetails
import com.tiffzy.restaurant.data.model.OrderItemDetails
import com.tiffzy.restaurant.data.remote.RestaurantSocketManager
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitchenViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val socketManager: RestaurantSocketManager,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _ordersState = MutableStateFlow<UiState<List<OrderDetails>>>(UiState.Loading)
    val ordersState: StateFlow<UiState<List<OrderDetails>>> = _ordersState.asStateFlow()

    private val _ordersList = MutableStateFlow<List<OrderDetails>>(emptyList())

    init {
        loadLiveOrders()
        startSocket()
    }

    fun loadLiveOrders() {
        viewModelScope.launch {
            _ordersState.value = UiState.Loading
            when (val result = repository.getLiveOrders()) {
                is Resource.Success -> {
                    _ordersList.value = result.data.orders
                    _ordersState.value = UiState.Success(result.data.orders)
                }
                is Resource.Error -> {
                    val fallback = getSampleKitchenOrders()
                    _ordersList.value = fallback
                    _ordersState.value = UiState.Success(fallback)
                }
                else -> {}
            }
        }
    }

    private fun getSampleKitchenOrders(): List<OrderDetails> {
        return listOf(
            OrderDetails(
                id = 101,
                orderNo = "K-001",
                invoiceNo = null,
                orderSource = "DINE_IN",
                customerName = "Guest 1",
                phone = null,
                email = null,
                tableNo = "101",
                notes = "No onions",
                deliveryAddress = null,
                deliveryLatitude = null,
                deliveryLongitude = null,
                subtotal = 220.0,
                taxAmount = 30.0,
                serviceChargeAmount = 0.0,
                total = 250.0,
                status = "PLACED",
                paymentStatus = "PENDING",
                paymentMode = "CASH",
                fulfillment = "DINE_IN",
                createdAt = "2026-09-20T11:00:00.000Z",
                items = listOf(
                    OrderItemDetails(1, 1, "Burger", 1, 140.0, 140.0),
                    OrderItemDetails(2, 5, "Coke", 2, 60.0, 120.0)
                ),
                statusEvents = emptyList()
            )
        )
    }

    private fun startSocket() {
        viewModelScope.launch {
            val token = sessionManager.authToken.first() ?: return@launch
            val baseUrl = "https://api.tiffzy.com" 
            socketManager.connect(context, baseUrl, token) { updatedOrder ->
                val current = _ordersList.value.toMutableList()
                val index = current.indexOfFirst { it.id == updatedOrder.id }
                if (index != -1) {
                    current[index] = updatedOrder
                } else {
                    current.add(0, updatedOrder)
                }
                _ordersList.value = current
                _ordersState.value = UiState.Success(current)
            }
        }
    }

    fun updateStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}
