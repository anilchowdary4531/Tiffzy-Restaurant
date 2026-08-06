package com.tiffzy.restaurant.ui.home.orders

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.CustomerOrderGroupsResponse
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _ordersState = MutableStateFlow<UiState<CustomerOrderGroupsResponse>>(UiState.Loading)
    val ordersState: StateFlow<UiState<CustomerOrderGroupsResponse>> = _ordersState.asStateFlow()

    init {
        fetchOrders()
    }

    fun fetchOrders() {
        viewModelScope.launch {
            _ordersState.value = UiState.Loading
            val phone = sessionManager.userPhone.first() ?: ""
            when (val result = repository.getCustomerOrders(phone)) {
                is Resource.Success -> _ordersState.value = UiState.Success(result.data)
                is Resource.Error -> _ordersState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun reorder(orderId: Int, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.reorder(orderId)) {
                is Resource.Success -> onSuccess(result.data.order.orderNo)
                is Resource.Error -> { /* Handle error toast */ }
                else -> {}
            }
        }
    }
}
