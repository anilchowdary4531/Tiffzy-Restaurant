package com.tiffzy.restaurant.ui.home.cart

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.repository.AuthRepository
import com.tiffzy.restaurant.data.repository.CartRepository
import com.tiffzy.restaurant.data.repository.LocationRepository
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val restaurantRepository: RestaurantRepository,
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository
) : BaseViewModel() {

    val cart = cartRepository.cart

    private val _orderState = MutableStateFlow<UiState<OrderResponse>>(UiState.Idle)
    val orderState: StateFlow<UiState<OrderResponse>> = _orderState.asStateFlow()

    private val _addresses = MutableStateFlow<List<Address>>(emptyList())
    val addresses: StateFlow<List<Address>> = _addresses.asStateFlow()

    private val _selectedAddress = MutableStateFlow<Address?>(null)
    val selectedAddress: StateFlow<Address?> = _selectedAddress.asStateFlow()

    private val _walletBalance = MutableStateFlow(0.0)
    val walletBalance: StateFlow<Double> = _walletBalance.asStateFlow()

    var deliveryInstructions = MutableStateFlow("")
    var paymentMethod = MutableStateFlow("cod") // cod | online | wallet
    var useWallet = MutableStateFlow(false)

    init {
        fetchAddresses()
        fetchProfile()
    }

    private fun fetchAddresses() {
        viewModelScope.launch {
            when (val result = locationRepository.getSavedAddresses()) {
                is Resource.Success -> {
                    _addresses.value = result.data
                    _selectedAddress.value = result.data.find { it.isDefault } ?: result.data.firstOrNull()
                }
                else -> {}
            }
        }
    }

    private fun fetchProfile() {
        viewModelScope.launch {
            when (val result = authRepository.getProfile()) {
                is Resource.Success -> {
                    _walletBalance.value = result.data.customer.walletBalance
                }
                else -> {}
            }
        }
    }

    fun selectAddress(address: Address) {
        _selectedAddress.value = address
    }

    fun placeOrder() {
        val currentCart = cart.value
        val slug = currentCart.restaurantSlug ?: return
        val address = _selectedAddress.value ?: return

        viewModelScope.launch {
            _orderState.value = UiState.Loading
            val request = OrderRequest(
                restaurantSlug = slug,
                items = currentCart.items.map { 
                    OrderItemRequest(
                        menuItemId = it.menuItem.id,
                        quantity = it.quantity,
                        variantId = it.selectedVariant?.id,
                        addOnIds = it.selectedAddOns.map { addOn -> addOn.id }
                    )
                },
                deliveryAddressId = address.id,
                deliveryInstructions = deliveryInstructions.value,
                couponCode = currentCart.appliedCoupon?.code,
                paymentMethod = paymentMethod.value,
                useWallet = useWallet.value,
                subtotal = currentCart.subtotal,
                taxAmount = currentCart.gstAmount,
                deliveryCharge = currentCart.deliveryCharge,
                packingCharge = currentCart.packingCharge,
                totalAmount = currentCart.grandTotal
            )

            when (val result = restaurantRepository.placeOrder(slug, request)) {
                is Resource.Success -> {
                    _orderState.value = UiState.Success(result.data)
                    cartRepository.clearCart()
                }
                is Resource.Error -> {
                    _orderState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }
}
