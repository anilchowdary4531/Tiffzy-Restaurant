package com.tiffzy.restaurant.data.repository

import com.tiffzy.restaurant.data.model.Cart
import com.tiffzy.restaurant.data.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor() {
    private val _cart = MutableStateFlow(Cart())
    val cart: StateFlow<Cart> = _cart.asStateFlow()

    fun addToCart(item: CartItem) {
        val currentCart = _cart.value
        
        // If adding from a different restaurant, clear cart first (standard food app behavior)
        if (currentCart.restaurantSlug != null && currentCart.restaurantSlug != item.restaurantSlug) {
            _cart.value = Cart(items = listOf(item), restaurantSlug = item.restaurantSlug)
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

        _cart.value = Cart(items = items, restaurantSlug = item.restaurantSlug)
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
        _cart.value = Cart(items = items, restaurantSlug = slug)
    }

    fun clearCart() {
        _cart.value = Cart()
    }
}
