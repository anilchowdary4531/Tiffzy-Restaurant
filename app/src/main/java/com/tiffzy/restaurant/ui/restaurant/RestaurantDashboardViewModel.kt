package com.tiffzy.restaurant.ui.restaurant

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.AnalyticsResponse
import com.tiffzy.restaurant.data.model.RestaurantSettings
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardUiState {
    object Idle : DashboardUiState()
    object Loading : DashboardUiState()
    data class Success(
        val analytics: AnalyticsResponse,
        val settings: RestaurantSettings
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

@HiltViewModel
class RestaurantDashboardViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Idle)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                val restaurantId = ridString.toInt()
                
                val analyticsResource = repository.getRestaurantAnalytics(restaurantId, "24h")
                val settingsResource = repository.getRestaurantSettings(restaurantId)

                if (analyticsResource is Resource.Success && settingsResource is Resource.Success) {
                    _uiState.value = DashboardUiState.Success(
                        analyticsResource.data,
                        settingsResource.data.restaurant
                    )
                } else {
                    val errorMsg = when {
                        analyticsResource is Resource.Error -> analyticsResource.message
                        settingsResource is Resource.Error -> settingsResource.message
                        else -> "Failed to load dashboard data"
                    }
                    _uiState.value = DashboardUiState.Error(errorMsg)
                }
            } else {
                _uiState.value = DashboardUiState.Error("Unauthorized: No restaurant linked to this account.")
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            sessionManager.logout()
            onLoggedOut()
        }
    }

    fun toggleRestaurantStatus(currentIsActive: Boolean) {
        viewModelScope.launch {
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                val restaurantId = ridString.toInt()
                repository.updateRestaurantSettings(restaurantId, com.tiffzy.restaurant.data.model.RestaurantSettingsUpdateRequest(isActive = !currentIsActive))
                loadDashboard()
            }
        }
    }
}
