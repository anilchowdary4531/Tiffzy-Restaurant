package com.tiffzy.restaurant.ui.restaurant.supply

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplyViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _productsState = MutableStateFlow<UiState<List<SupplyProduct>>>(UiState.Loading)
    val productsState: StateFlow<UiState<List<SupplyProduct>>> = _productsState.asStateFlow()

    private val _ordersState = MutableStateFlow<UiState<List<SupplyOrder>>>(UiState.Loading)
    val ordersState: StateFlow<UiState<List<SupplyOrder>>> = _ordersState.asStateFlow()

    init {
        loadProducts()
        loadOrders()
    }

    fun loadProducts(query: String? = null, category: String? = null) {
        viewModelScope.launch {
            _productsState.value = UiState.Loading
            when (val result = repository.getSupplyProducts(query, category)) {
                is Resource.Success -> {
                    val list = result.data.products
                    _productsState.value = UiState.Success(list)
                }
                is Resource.Error -> {
                    _productsState.value = UiState.Success(getSampleProducts())
                }
                else -> {}
            }
        }
    }

    private fun getSampleProducts(): List<SupplyProduct> {
        return listOf(
            SupplyProduct(1, "Organic Tomatoes", "Fresh red tomatoes", null, 40.0, "KG", "Vegetables", "Green Farms"),
            SupplyProduct(2, "Refined Sunflower Oil", "Premium quality oil", null, 120.0, "Litre", "Grocery", "Sunlight Ltd"),
            SupplyProduct(3, "Basmati Rice", "Extra long grain", null, 85.0, "KG", "Grocery", "Himalayan Rice")
        )
    }

    fun loadOrders() {
        viewModelScope.launch {
            _ordersState.value = UiState.Loading
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            when (val result = repository.getSupplyOrders(rid)) {
                is Resource.Success -> {
                    val list = result.data.orders
                    _ordersState.value = UiState.Success(list)
                }
                is Resource.Error -> {
                    _ordersState.value = UiState.Success(getSampleSupplyOrders())
                }
                else -> {}
            }
        }
    }

    private fun getSampleSupplyOrders(): List<SupplyOrder> {
        return listOf(
            SupplyOrder(1, "SUP-8891", "DELIVERED", 4500.0, 12, "2026-09-18"),
            SupplyOrder(2, "SUP-8892", "IN_TRANSIT", 2100.0, 5, "2026-09-19")
        )
    }
}
