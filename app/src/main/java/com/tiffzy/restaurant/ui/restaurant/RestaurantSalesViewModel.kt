package com.tiffzy.restaurant.ui.restaurant

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.AnalyticsResponse
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SalesUiState {
    object Loading : SalesUiState()
    data class Success(val analytics: AnalyticsResponse) : SalesUiState()
    data class Error(val message: String) : SalesUiState()
}

@HiltViewModel
class RestaurantSalesViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<SalesUiState>(SalesUiState.Loading)
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    private val _currentRange = MutableStateFlow("24h")
    val currentRange: StateFlow<String> = _currentRange.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics(range: String = "24h") {
        _currentRange.value = range
        viewModelScope.launch {
            _uiState.value = SalesUiState.Loading
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                val restaurantId = ridString.toInt()
                when (val result = repository.getRestaurantAnalytics(restaurantId, range)) {
                    is Resource.Success -> {
                        _uiState.value = SalesUiState.Success(result.data)
                    }
                    is Resource.Error -> {
                        _uiState.value = SalesUiState.Error(result.message)
                    }
                    else -> {}
                }
            } else {
                _uiState.value = SalesUiState.Error("Unauthorized")
            }
        }
    }
}
