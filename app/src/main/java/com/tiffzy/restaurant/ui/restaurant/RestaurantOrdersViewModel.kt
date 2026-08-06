package com.tiffzy.restaurant.ui.restaurant

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.BuildConfig
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.OrderDetails
import com.tiffzy.restaurant.data.remote.RestaurantSocketManager
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RestaurantOrdersUiState {
    object Loading : RestaurantOrdersUiState()
    data class Success(val orders: List<OrderDetails>) : RestaurantOrdersUiState()
    data class Error(val message: String) : RestaurantOrdersUiState()
}

@HiltViewModel
class RestaurantOrdersViewModel @Inject constructor(
    private val application: Application,
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager,
    private val socketManager: RestaurantSocketManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<RestaurantOrdersUiState>(RestaurantOrdersUiState.Loading)
    val uiState: StateFlow<RestaurantOrdersUiState> = _uiState.asStateFlow()

    private val _ordersList = MutableStateFlow<List<OrderDetails>>(emptyList())

    init {
        loadOrders()
        connectSocket()
    }

    fun loadOrders(status: String? = null) {
        viewModelScope.launch {
            _uiState.value = RestaurantOrdersUiState.Loading
            when (val result = repository.getLiveOrders(status)) {
                is Resource.Success -> {
                    _ordersList.value = result.data.orders
                    _uiState.value = RestaurantOrdersUiState.Success(_ordersList.value)
                }
                is Resource.Error -> {
                    _uiState.value = RestaurantOrdersUiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    private fun connectSocket() {
        viewModelScope.launch {
            val token = sessionManager.authToken.first()
            if (token != null) {
                socketManager.connect(application, BuildConfig.BASE_URL, token) { updatedOrder ->
                    updateOrderInList(updatedOrder)
                }
            }
        }
    }

    private fun updateOrderInList(updatedOrder: OrderDetails) {
        val current = _ordersList.value.toMutableList()
        val index = current.indexOfFirst { it.id == updatedOrder.id }
        if (index != -1) {
            current[index] = updatedOrder
        } else {
            current.add(0, updatedOrder)
        }
        _ordersList.value = current
        _uiState.value = RestaurantOrdersUiState.Success(current)
    }

    fun updateStatus(orderId: Int, nextStatus: String) {
        viewModelScope.launch {
            when (val result = repository.updateOrderStatus(orderId, nextStatus)) {
                is Resource.Success -> {
                    updateOrderInList(result.data.order)
                }
                else -> {}
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}
