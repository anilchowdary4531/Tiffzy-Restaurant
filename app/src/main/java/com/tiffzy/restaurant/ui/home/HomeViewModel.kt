package com.tiffzy.restaurant.ui.home

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.Category
import com.tiffzy.restaurant.data.model.HomeResponse
import com.tiffzy.restaurant.data.model.Restaurant
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RestaurantRepository
) : BaseViewModel() {

    private val _homeState = MutableStateFlow<UiState<HomeResponse>>(UiState.Idle)
    val homeState: StateFlow<UiState<HomeResponse>> = _homeState.asStateFlow()

    private val _currentLocation = MutableStateFlow("Determining location...")
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    // Paging for Nearby Restaurants
    @OptIn(ExperimentalCoroutinesApi::class)
    val nearbyRestaurants: Flow<PagingData<Restaurant>> = _selectedCategory.flatMapLatest { category ->
        repository.getNearbyRestaurants(17.3850, 78.4867, category?.name)
    }.cachedIn(viewModelScope)

    init {
        refreshHome()
    }

    fun refreshHome() {
        viewModelScope.launch {
            _homeState.value = UiState.Loading
            when (val result = repository.getHomeData()) {
                is Resource.Success -> _homeState.value = UiState.Success(result.data)
                is Resource.Error -> _homeState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryClick(category: Category) {
        if (_selectedCategory.value?.id == category.id) {
            _selectedCategory.value = null // Deselect if same
        } else {
            _selectedCategory.value = category
        }
    }

    fun updateLocation(location: String) {
        _currentLocation.value = location
    }
}
