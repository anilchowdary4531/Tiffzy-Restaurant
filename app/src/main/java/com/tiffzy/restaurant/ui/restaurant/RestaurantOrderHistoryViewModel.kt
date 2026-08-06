package com.tiffzy.restaurant.ui.restaurant

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.OrderDetails
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrderHistoryUiState {
    object Loading : OrderHistoryUiState()
    data class Success(val orders: List<OrderDetails>) : OrderHistoryUiState()
    data class Error(val message: String) : OrderHistoryUiState()
}

@HiltViewModel
class RestaurantOrderHistoryViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<OrderHistoryUiState>(OrderHistoryUiState.Loading)
    val uiState: StateFlow<OrderHistoryUiState> = _uiState.asStateFlow()

    private val _currentStatusFilter = MutableStateFlow<String?>(null)
    val currentStatusFilter: StateFlow<String?> = _currentStatusFilter.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory(status: String? = null, query: String? = null) {
        _currentStatusFilter.value = status
        viewModelScope.launch {
            _uiState.value = OrderHistoryUiState.Loading
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                val restaurantId = ridString.toInt()
                when (val result = repository.getOwnerOrders(
                    restaurantId = restaurantId,
                    status = status,
                    query = query
                )) {
                    is Resource.Success -> {
                        _uiState.value = OrderHistoryUiState.Success(result.data.orders)
                    }
                    is Resource.Error -> {
                        _uiState.value = OrderHistoryUiState.Error(result.message)
                    }
                    else -> {}
                }
            } else {
                _uiState.value = OrderHistoryUiState.Error("Unauthorized")
            }
        }
    }
}
