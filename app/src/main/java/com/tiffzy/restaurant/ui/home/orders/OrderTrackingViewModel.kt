package com.tiffzy.restaurant.ui.home.orders

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.BuildConfig
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.OrderDetails
import com.tiffzy.restaurant.data.remote.CustomerSocketManager
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
class OrderTrackingViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val socketManager: CustomerSocketManager,
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val orderId: Int = checkNotNull(savedStateHandle["orderId"])

    private val _orderState = MutableStateFlow<UiState<OrderDetails>>(UiState.Loading)
    val orderState: StateFlow<UiState<OrderDetails>> = _orderState.asStateFlow()

    init {
        fetchOrderDetails()
        connectSocket()
    }

    fun fetchOrderDetails() {
        viewModelScope.launch {
            _orderState.value = UiState.Loading
            when (val result = repository.getOrderDetails(orderId)) {
                is Resource.Success -> _orderState.value = UiState.Success(result.data.order)
                is Resource.Error -> _orderState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    private fun connectSocket() {
        viewModelScope.launch {
            val token = sessionManager.authToken.first() ?: return@launch
            socketManager.connect(context, BuildConfig.BASE_URL, token) { updatedOrder ->
                if (updatedOrder.id == orderId) {
                    _orderState.value = UiState.Success(updatedOrder)
                }
            }
        }
    }

    fun cancelOrder() {
        viewModelScope.launch {
            when (val result = repository.cancelOrder(orderId)) {
                is Resource.Success -> fetchOrderDetails()
                is Resource.Error -> { /* Show error */ }
                else -> {}
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.disconnect()
    }
}
