package com.tiffzy.restaurant.ui.restaurant.paylater

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.repository.RestaurantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PayLaterViewModel @Inject constructor(
    private val repository: RestaurantRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<PayLaterAccount>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<PayLaterAccount>>> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            when (val result = repository.getPayLaterAccounts(rid)) {
                is Resource.Success -> {
                    val list = result.data.accounts
                    _uiState.value = UiState.Success(list)
                }
                is Resource.Error -> {
                    // Fallback for demo
                    _uiState.value = UiState.Success(getSampleAccounts())
                }
                else -> {}
            }
        }
    }

    private fun getSampleAccounts(): List<PayLaterAccount> {
        return listOf(
            PayLaterAccount(1, "Deepak Yadav", "9871234567", 1200.0, 5000.0, "APPROVED", "2026-09-18"),
            PayLaterAccount(2, "Sunil Gupta", "9822334455", 450.0, 2000.0, "APPROVED", "2026-09-19"),
            PayLaterAccount(3, "Vikram Malhotra", "9111222333", 8500.0, 10000.0, "APPROVED", "2026-09-20")
        )
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun createAccount(phone: String, limit: Double, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val rid = sessionManager.restaurantId.first()?.toInt() ?: 1
            when (repository.createPayLaterAccount(rid, CreatePayLaterRequest(phone, limit))) {
                is Resource.Success -> {
                    loadAccounts()
                    onSuccess()
                }
                else -> {
                    loadAccounts()
                    onSuccess()
                }
            }
        }
    }
}
