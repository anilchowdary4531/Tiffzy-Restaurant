package com.tiffzy.restaurant.ui.restaurant

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.RestaurantSettings
import com.tiffzy.restaurant.data.model.RestaurantSettingsUpdateRequest
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Loading : SettingsUiState()
    data class Success(val settings: RestaurantSettings) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

@HiltViewModel
class RestaurantSettingsViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                when (val result = repository.getRestaurantSettings(ridString.toInt())) {
                    is Resource.Success -> {
                        _uiState.value = SettingsUiState.Success(result.data.restaurant)
                    }
                    is Resource.Error -> {
                        _uiState.value = SettingsUiState.Error(result.message)
                    }
                    else -> {}
                }
            } else {
                _uiState.value = SettingsUiState.Error("Unauthorized")
            }
        }
    }

    fun updateSettings(request: RestaurantSettingsUpdateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val ridString = sessionManager.restaurantId.first()
            if (ridString != null) {
                val result = repository.updateRestaurantSettings(ridString.toInt(), request)
                if (result is Resource.Success) {
                    loadSettings()
                    onSuccess()
                }
            }
            _isSaving.value = false
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            sessionManager.logout()
            onLoggedOut()
        }
    }
}
