package com.tiffzy.restaurant.ui.home.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.model.RestaurantDetailResponse
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RestaurantDetailViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel() {

    private val slug: String = checkNotNull(savedStateHandle["slug"])

    private val _uiState = MutableStateFlow<UiState<RestaurantDetailResponse>>(UiState.Loading)
    val uiState: StateFlow<UiState<RestaurantDetailResponse>> = _uiState.asStateFlow()

    init {
        fetchRestaurantDetails()
    }

    fun fetchRestaurantDetails() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = repository.getRestaurantDetails(slug)) {
                is Resource.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is Resource.Error -> {
                    _uiState.value = UiState.Error(result.message)
                }
                else -> {}
            }
        }
    }

    fun toggleFavorite() {
        // TODO: Implement toggle favorite API
    }
}
