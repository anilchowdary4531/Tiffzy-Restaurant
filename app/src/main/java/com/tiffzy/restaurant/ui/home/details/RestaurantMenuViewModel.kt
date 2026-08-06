package com.tiffzy.restaurant.ui.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.repository.CartRepository
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantMenuViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _menuState = MutableStateFlow<UiState<RestaurantMenuResponse>>(UiState.Loading)
    val menuState: StateFlow<UiState<RestaurantMenuResponse>> = _menuState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _isVegOnly = MutableStateFlow(false)
    val isVegOnly = _isVegOnly.asStateFlow()

    val cart = cartRepository.cart

    val filteredMenu = combine(
        _menuState,
        _searchQuery,
        _selectedCategory,
        _isVegOnly
    ) { state, query, category, vegOnly ->
        if (state is UiState.Success) {
            state.data.menu.filter { item ->
                val matchesQuery = item.name.contains(query, ignoreCase = true) ||
                                  item.description?.contains(query, ignoreCase = true) == true
                val matchesCategory = category == null || item.category == category
                val matchesVeg = !vegOnly || item.isVeg
                matchesQuery && matchesCategory && matchesVeg
            }
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories = _menuState.map { state ->
        if (state is UiState.Success) {
            state.data.menu.map { it.category }.distinct()
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        fetchMenu()
    }

    fun fetchMenu() {
        viewModelScope.launch {
            _menuState.value = UiState.Loading
            when (val result = repository.getRestaurantMenu(slug)) {
                is Resource.Success -> _menuState.value = UiState.Success(result.data)
                is Resource.Error -> _menuState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleVegOnly() {
        _isVegOnly.value = !_isVegOnly.value
    }

    fun addToCart(item: MenuItem, variant: MenuVariant? = null, addOns: List<AddOn> = emptyList()) {
        val cartItem = CartItem(
            menuItem = item,
            quantity = 1,
            selectedVariant = variant,
            selectedAddOns = addOns,
            restaurantSlug = slug,
            restaurantName = (_menuState.value as? UiState.Success)?.data?.restaurant?.name ?: ""
        )
        cartRepository.addToCart(cartItem)
    }

    fun removeFromCart(item: MenuItem, variant: MenuVariant? = null, addOns: List<AddOn> = emptyList()) {
        val cartItem = CartItem(
            menuItem = item,
            quantity = 1,
            selectedVariant = variant,
            selectedAddOns = addOns,
            restaurantSlug = slug,
            restaurantName = ""
        )
        cartRepository.removeFromCart(cartItem)
    }
    
    fun getItemQuantity(itemId: Int): Int {
        return cart.value.items
            .filter { it.menuItem.id == itemId }
            .sumOf { it.quantity }
    }
}
