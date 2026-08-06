package com.tiffzy.restaurant.ui.home.cart

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.CartItem
import com.tiffzy.restaurant.data.model.Coupon
import com.tiffzy.restaurant.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CartRepository
) : BaseViewModel() {

    val cart = repository.cart
    
    private val _couponState = MutableStateFlow<UiState<Coupon>>(UiState.Idle)
    val couponState: StateFlow<UiState<Coupon>> = _couponState.asStateFlow()

    fun addItem(item: CartItem) {
        repository.addToCart(item.copy(quantity = 1))
    }

    fun removeItem(item: CartItem) {
        repository.removeFromCart(item)
    }

    fun applyCoupon(code: String) {
        if (code.isEmpty()) return
        viewModelScope.launch {
            _couponState.value = UiState.Loading
            when (val result = repository.applyCoupon(code)) {
                is Resource.Success -> _couponState.value = UiState.Success(result.data)
                is Resource.Error -> _couponState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun removeCoupon() {
        repository.removeCoupon()
        _couponState.value = UiState.Idle
    }
    
    fun clearCart() {
        repository.clearCart()
    }
}
