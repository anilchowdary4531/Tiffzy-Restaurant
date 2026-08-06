package com.tiffzy.restaurant.data.repository

import com.tiffzy.restaurant.core.base.BaseRepository
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    private val apiService: ApiService
) : BaseRepository() {
    private val _cart = MutableStateFlow(Cart())
    val cart: StateFlow<Cart> = _cart.asStateFlow()

    fun addToCart(item: CartItem) {
        val currentCart = _cart.value
        
        if (currentCart.restaurantSlug != null && currentCart.restaurantSlug != item.restaurantSlug) {
            _cart.value = Cart(
                items = listOf(item), 
                restaurantSlug = item.restaurantSlug,
                restaurantName = item.restaurantName
            )
            return
        }

        val items = currentCart.items.toMutableList()
        val index = items.indexOfFirst { 
            it.menuItem.id == item.menuItem.id && 
            it.selectedVariant?.id == item.selectedVariant?.id &&
            it.selectedAddOns.map { a -> a.id }.sorted() == item.selectedAddOns.map { a -> a.id }.sorted()
        }

        if (index != -1) {
            val existing = items[index]
            items[index] = existing.copy(quantity = existing.quantity + item.quantity)
        } else {
            items.add(item)
        }

        _cart.value = currentCart.copy(
            items = items, 
            restaurantSlug = item.restaurantSlug,
            restaurantName = item.restaurantName
        )
    }

    fun removeFromCart(item: CartItem) {
        val currentCart = _cart.value
        val items = currentCart.items.toMutableList()
        val index = items.indexOfFirst { 
            it.menuItem.id == item.menuItem.id && 
            it.selectedVariant?.id == item.selectedVariant?.id &&
            it.selectedAddOns.map { a -> a.id }.sorted() == item.selectedAddOns.map { a -> a.id }.sorted()
        }

        if (index != -1) {
            val existing = items[index]
            if (existing.quantity > 1) {
                items[index] = existing.copy(quantity = existing.quantity - 1)
            } else {
                items.removeAt(index)
            }
        }

        val slug = if (items.isEmpty()) null else currentCart.restaurantSlug
        val name = if (items.isEmpty()) null else currentCart.restaurantName
        _cart.value = currentCart.copy(items = items, restaurantSlug = slug, restaurantName = name)
    }

    suspend fun applyCoupon(code: String): Resource<Coupon> {
        val currentCart = _cart.value
        if (currentCart.restaurantSlug == null) return Resource.Error("Cart is empty")
        
        val result = safeApiCall { 
            apiService.applyCoupon(CouponRequest(code, currentCart.subtotal, currentCart.restaurantSlug))
        }
        
        if (result is Resource.Success) {
            _cart.value = _cart.value.copy(appliedCoupon = result.data)
        }
        
        return result
    }

    fun removeCoupon() {
        _cart.value = _cart.value.copy(appliedCoupon = null)
    }

    fun clearCart() {
        _cart.value = Cart()
    }
}
