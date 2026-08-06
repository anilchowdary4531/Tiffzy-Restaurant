package com.tiffzy.restaurant.ui.home.profile

import androidx.lifecycle.viewModelScope
import com.tiffzy.restaurant.core.base.BaseViewModel
import com.tiffzy.restaurant.core.base.UiState
import com.tiffzy.restaurant.core.result.Resource
import com.tiffzy.restaurant.data.local.SessionManager
import com.tiffzy.restaurant.data.model.*
import com.tiffzy.restaurant.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val sessionManager: SessionManager
) : BaseViewModel() {

    private val _profileState = MutableStateFlow<UiState<Customer>>(UiState.Loading)
    val profileState: StateFlow<UiState<Customer>> = _profileState.asStateFlow()

    private val _walletState = MutableStateFlow<UiState<WalletHistoryResponse>>(UiState.Loading)
    val walletState: StateFlow<UiState<WalletHistoryResponse>> = _walletState.asStateFlow()

    private val _cardsState = MutableStateFlow<UiState<List<SavedCard>>>(UiState.Loading)
    val cardsState: StateFlow<UiState<List<SavedCard>>> = _cardsState.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            when (val result = repository.getProfile()) {
                is Resource.Success -> {
                    _profileState.value = UiState.Success(result.data.customer)
                    // Sync with session manager
                    sessionManager.saveUserInfo(
                        result.data.customer.name,
                        result.data.customer.phone,
                        "customer"
                    )
                }
                is Resource.Error -> _profileState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            val request = UpdateProfileRequest(name = name, email = email)
            when (val result = repository.updateProfile(request)) {
                is Resource.Success -> fetchProfile()
                is Resource.Error -> _profileState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun uploadPicture(file: MultipartBody.Part) {
        viewModelScope.launch {
            when (val result = repository.uploadProfilePicture(file)) {
                is Resource.Success -> fetchProfile()
                is Resource.Error -> { /* Show error toast */ }
                else -> {}
            }
        }
    }

    fun fetchWalletHistory() {
        viewModelScope.launch {
            _walletState.value = UiState.Loading
            when (val result = repository.getWalletHistory()) {
                is Resource.Success -> _walletState.value = UiState.Success(result.data)
                is Resource.Error -> _walletState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun fetchSavedCards() {
        viewModelScope.launch {
            _cardsState.value = UiState.Loading
            when (val result = repository.getSavedCards()) {
                is Resource.Success -> _cardsState.value = UiState.Success(result.data)
                is Resource.Error -> _cardsState.value = UiState.Error(result.message)
                else -> {}
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (val result = repository.deleteAccount()) {
                is Resource.Success -> {
                    logout(onSuccess)
                }
                is Resource.Error -> { /* Show error toast */ }
                else -> {}
            }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            sessionManager.logout()
            onSuccess()
        }
    }
}
